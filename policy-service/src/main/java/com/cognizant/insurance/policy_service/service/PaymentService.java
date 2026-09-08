package com.cognizant.insurance.policy_service.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cognizant.insurance.policy_service.dto.PaymentRequest;
import com.cognizant.insurance.policy_service.entity.Payment;
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
        // Make sure the policy exists before we take a payment against it.
        policyService.getById(request.getPolicyId());

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
        return paymentRepository.findByPolicyId(policyId);
    }
}
