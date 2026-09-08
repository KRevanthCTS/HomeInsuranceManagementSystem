package com.cognizant.insurance.policy_service.service;

import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cognizant.insurance.policy_service.client.CustomerClient;
import com.cognizant.insurance.policy_service.client.CustomerDto;
import com.cognizant.insurance.policy_service.entity.Policy;
import com.cognizant.insurance.policy_service.entity.Property;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;

// Builds the mock policy document described in the brief. It pulls the customer
// details from customer-service (via Feign) and lays everything out with OpenPDF.
@Service
public class PolicyPdfService {

    private static final Logger log = LoggerFactory.getLogger(PolicyPdfService.class);

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
    private static final Font HEADING_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font TEXT_FONT = FontFactory.getFont(FontFactory.HELVETICA, 11);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);

    private final PolicyService policyService;
    private final PropertyService propertyService;
    private final PremiumCalculator premiumCalculator;
    private final CustomerClient customerClient;

    public PolicyPdfService(PolicyService policyService,
            PropertyService propertyService,
            PremiumCalculator premiumCalculator,
            CustomerClient customerClient) {
        this.policyService = policyService;
        this.propertyService = propertyService;
        this.premiumCalculator = premiumCalculator;
        this.customerClient = customerClient;
    }

    public byte[] generate(Long policyId) {
        Policy policy = policyService.getById(policyId);
        Property property = propertyService.getById(policy.getPropertyId());
        CustomerDto customer = fetchCustomerSafely(property.getCustomerId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 48, 48, 48, 48);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // ---- Header ----
            Paragraph company = new Paragraph("Cognizant Home Insurance", TITLE_FONT);
            company.setAlignment(Element.ALIGN_CENTER);
            document.add(company);

            Paragraph title = new Paragraph("Home Insurance Policy Document", HEADING_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(6);
            document.add(title);

            Paragraph number = new Paragraph("Policy Number: " + policy.getPolicyNumber(), TEXT_FONT);
            number.setAlignment(Element.ALIGN_CENTER);
            number.setSpacingAfter(16);
            document.add(number);

            // ---- Customer info ----
            document.add(sectionHeading("Customer Details"));
            if (customer != null) {
                document.add(line("Name", customer.getFullName()));
                document.add(line("Age", String.valueOf(customer.getAge())));
                document.add(line("Contact", customer.getPhoneNumber()));
                document.add(line("Address", customer.getAddress()));
            } else {
                document.add(line("Customer Id", String.valueOf(property.getCustomerId())));
                document.add(new Paragraph("(customer details service was unavailable)", SMALL_FONT));
            }

            // ---- Property info ----
            document.add(sectionHeading("Property Details"));
            document.add(line("Type", String.valueOf(property.getPropertyType())));
            document.add(line("Built-up Area", property.getBuiltUpArea() + " sq.ft"));
            document.add(line("Construction Year", String.valueOf(property.getConstructionYear())));
            document.add(line("Estimated Value", "Rs. " + property.getPropertyValue()));
            document.add(line("Location", formatAddress(property)));

            // ---- Coverage ----
            document.add(sectionHeading("Coverage Details"));
            document.add(line("Coverage Type", policy.getPolicyType()));
            document.add(line("Coverage Amount", "Rs. " + policy.getCoverageAmount()));
            document.add(line("Start Date", String.valueOf(policy.getStartDate())));
            document.add(line("End Date", String.valueOf(policy.getEndDate())));
            document.add(line("Status", String.valueOf(policy.getStatus())));

            // ---- Premium ----
            document.add(sectionHeading("Premium Calculation"));
            document.add(new Paragraph(
                    "Premium = Base Rate (0.5%) x Property Value x Risk Factor", TEXT_FONT));
            document.add(line("Risk Factor Applied", String.valueOf(premiumCalculator.riskFactorFor(property))));
            document.add(line("Annual Premium", "Rs. " + policy.getPremiumAmount()));

            // ---- Terms ----
            document.add(sectionHeading("Terms & Conditions"));
            Paragraph terms = new Paragraph(
                    "This policy covers loss or damage to the insured property as per the coverage "
                    + "type stated above, subject to verification of the claim. Claims must be reported "
                    + "within 30 days of the incident. The premium shown is payable annually. This is a "
                    + "system-generated mock document for demonstration purposes.", TEXT_FONT);
            terms.setSpacingAfter(24);
            document.add(terms);

            // ---- Footer ----
            Paragraph signatory = new Paragraph("Authorized Signatory", LABEL_FONT);
            signatory.setAlignment(Element.ALIGN_RIGHT);
            document.add(signatory);

            Paragraph footer = new Paragraph(
                    "Cognizant Home Insurance  |  support@cognizant-insurance.example  |  1800-000-000",
                    SMALL_FONT);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(12);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate policy PDF", e);
        }
        return out.toByteArray();
    }

    private CustomerDto fetchCustomerSafely(Long customerId) {
        try {
            return customerClient.getCustomerById(customerId);
        } catch (Exception e) {
            // The document is still useful without the name/contact, so don't fail the whole request.
            log.warn("Could not fetch customer {} for PDF: {}", customerId, e.getMessage());
            return null;
        }
    }

    private String formatAddress(Property p) {
        return String.join(", ",
                nullToEmpty(p.getBuildingNo()),
                nullToEmpty(p.getStreet()),
                nullToEmpty(p.getCity()),
                nullToEmpty(p.getState()),
                nullToEmpty(p.getZipCode())).replaceAll("(, )+", ", ").trim();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private Paragraph sectionHeading(String text) {
        Paragraph p = new Paragraph(text, HEADING_FONT);
        p.setSpacingBefore(12);
        p.setSpacingAfter(4);
        return p;
    }

    private Paragraph line(String label, String value) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(label + ": ", LABEL_FONT));
        p.add(new Phrase(value == null ? "-" : value, TEXT_FONT));
        return p;
    }
}
