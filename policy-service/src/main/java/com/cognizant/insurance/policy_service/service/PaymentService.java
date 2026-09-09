package com.cognizant.insurance.policy_service.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cognizant.insurance.policy_service.dto.PaymentRequest;
import com.cognizant.insurance.policy_service.entity.Payment;
import com.cognizant.insurance.policy_service.entity.Policy;
import com.cognizant.insurance.policy_service.exception.BadRequestException;
import com.cognizant.insurance.policy_service.exception.ConflictException;
import com.cognizant.insurance.policy_service.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PolicyService policyService;

    public PaymentService(PaymentRepository paymentRepository, PolicyService policyService) {
        this.paymentRepository = paymentRepository;
        this.policyService = policyService;
    }

    public Payment recordPayment(PaymentRequest request) {
        // Confirms the policy exists AND that the caller owns it.
        Policy policy = policyService.getById(request.getPolicyId());

        // A payment has to relate to what is actually owed, otherwise the policy
        // can be "paid" with 1 rupee or overpaid without limit.
        BigDecimal outstanding = outstandingFor(policy);
        if (outstanding.signum() <= 0) {
            throw new ConflictException("The premium for policy "
                    + policy.getPolicyNumber() + " is already paid in full");
        }
        if (request.getPaymentAmount().compareTo(outstanding) > 0) {
            throw new BadRequestException("paymentAmount " + request.getPaymentAmount()
                    + " exceeds the outstanding premium of " + outstanding);
        }

        Payment payment = new Payment();
        payment.setPolicyId(request.getPolicyId());
        payment.setPaymentAmount(request.getPaymentAmount());
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod(request.getPaymentMethod());
        // In a real system this would come back from a payment gateway.
        payment.setPaymentStatus("SUCCESS");

        return paymentRepository.save(payment);
    }

    public List<Payment> getByPolicy(Long policyId) {
        // Runs the same ownership check as reading the policy itself.
        policyService.getById(policyId);
        return paymentRepository.findByPolicyId(policyId);
    }

    // Annual premium minus everything already paid against this policy.
    private BigDecimal outstandingFor(Policy policy) {
        BigDecimal paid = paymentRepository.findByPolicyId(policy.getPolicyId()).stream()
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return policy.getPremiumAmount().subtract(paid);
    }
}
