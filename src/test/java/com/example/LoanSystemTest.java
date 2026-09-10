package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoanSystemTest {

    // ==========================================
    // 1. POSITIVE ARCHETYPE TEST SUITE
    // ==========================================
    
    @Test
    public void testPositive_LowRiskApprovalRoute() {
        Customer customer = new Customer("Aravind Kumar", 30, "GOV12345", 50000.0, 780, 5000.0);
        LoanApplication app = new LoanApplication("APP001", customer, 200000.0);
        CreditAssessment assessment = CreditAssessment.assessLoan(app);

        assertEquals(CreditAssessment.RiskStatus.LOW_RISK, assessment.getStatus());
        assertEquals(750000.0, assessment.getMaxPermissibleLoan()); 
        assertTrue(assessment.getRejectionReasons().isEmpty());
    }

    @Test
    public void testPositive_MediumRiskApprovalRoute() {
        Customer customer = new Customer("Sarah Jones", 28, "GOV67890", 40000.0, 660, 4000.0);
        LoanApplication app = new LoanApplication("APP002", customer, 100000.0);
        CreditAssessment assessment = CreditAssessment.assessLoan(app);

        assertEquals(CreditAssessment.RiskStatus.MEDIUM_RISK, assessment.getStatus());
        assertEquals(400000.0, assessment.getMaxPermissibleLoan()); 
    }

    // ==========================================
    // 2. BOUNDARY VALIDATIONS
    // ==========================================

    @Test
    public void testBoundary_ExactMinimumPermissibleAge() {
        Customer customer = new Customer("Young Professional", 21, "GOV2121", 30000.0, 760, 0.0);
        LoanApplication app = new LoanApplication("APP003", customer, 50000.0);
        CreditAssessment assessment = CreditAssessment.assessLoan(app);

        assertNotEquals(CreditAssessment.RiskStatus.HIGH_RISK_REJECTED, assessment.getStatus());
    }

    @Test
    public void testBoundary_ExactMinimumCreditScore() {
        Customer customer = new Customer("Borderline Credit Profile", 35, "GOV6000", 35000.0, 600, 2000.0);
        LoanApplication app = new LoanApplication("APP004", customer, 40000.0);
        CreditAssessment assessment = CreditAssessment.assessLoan(app);

        assertNotEquals(CreditAssessment.RiskStatus.HIGH_RISK_REJECTED, assessment.getStatus());
        assertEquals(175000.0, assessment.getMaxPermissibleLoan()); 
    }

    @Test
    public void testBoundary_ExactMaximumPermissibleDTI() {
        Customer customer = new Customer("Balanced Debt User", 40, "GOV5050", 40000.0, 700, 20000.0);
        LoanApplication app = new LoanApplication("APP005", customer, 100000.0);
        CreditAssessment assessment = CreditAssessment.assessLoan(app);

        assertNotEquals(CreditAssessment.RiskStatus.HIGH_RISK_REJECTED, assessment.getStatus());
    }

    // ==========================================
    // 3. NEGATIVE CRITERIA & AGGREGATED FAILURES
    // ==========================================

    @Test
    public void testNegative_MultipleFailuresAccumulated() {
        Customer customer = new Customer("High Risk Candidate", 20, "GOV0000", 15000.0, 550, 0.0);
        LoanApplication app = new LoanApplication("APP006", customer, 100000.0);
        CreditAssessment assessment = CreditAssessment.assessLoan(app);

        assertEquals(CreditAssessment.RiskStatus.HIGH_RISK_REJECTED, assessment.getStatus());
        assertTrue(assessment.getRejectionReasons().size() >= 3);
    }

    @Test
    public void testNegative_AmountExceedsPermissibleLimit() {
        Customer customer = new Customer("Overreaching Asker", 30, "GOV9999", 30000.0, 680, 2000.0);
        LoanApplication app = new LoanApplication("APP007", customer, 500000.0);
        CreditAssessment assessment = CreditAssessment.assessLoan(app);

        assertEquals(CreditAssessment.RiskStatus.HIGH_RISK_REJECTED, assessment.getStatus());
        assertTrue(assessment.getRejectionReasons().get(0).contains("exceeds the income-dependent maximum limit"));
    }

    // ==========================================
    // 4. INVALID DATA GUARDRAILS & EXCEPTIONS
    // ==========================================

    @Test
    public void testNegative_ValidationGuardrailsThrowExceptions() {
        assertThrows(InvalidApplicationException.class, () -> {
            new Customer("Error User", 25, " ", 30000.0, 700, 0.0);
        });

        assertThrows(InvalidApplicationException.class, () -> {
            new Customer("Error User", 25, "GOV111", -500.0, 700, 0.0);
        });
    }
}
