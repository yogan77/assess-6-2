package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

// Custom Runtime Exception for Input Validation Guardrails
class InvalidApplicationException extends RuntimeException {
    public InvalidApplicationException(String message) {
        super(message);
    }
}

// 1. Customer Model
class Customer {
    private final String name;
    private final int age;
    private final String govId;
    private final double monthlyIncome;
    private final int creditScore;
    private final double existingMonthlyObligations;

    public Customer(String name, int age, String govId, double monthlyIncome, int creditScore, double existingMonthlyObligations) {
        if (name == null || name.trim().isEmpty()) throw new InvalidApplicationException("Customer name cannot be empty.");
        if (age < 0) throw new InvalidApplicationException("Age cannot be negative.");
        if (govId == null || govId.trim().isEmpty()) throw new InvalidApplicationException("Government ID cannot be empty.");
        if (monthlyIncome < 0) throw new InvalidApplicationException("Monthly income cannot be negative.");
        if (creditScore < 300 || creditScore > 850) throw new InvalidApplicationException("Credit score must be between 300 and 850.");
        if (existingMonthlyObligations < 0) throw new InvalidApplicationException("Existing monthly obligations cannot be negative.");

        this.name = name;
        this.age = age;
        this.govId = govId;
        this.monthlyIncome = monthlyIncome;
        this.creditScore = creditScore;
        this.existingMonthlyObligations = existingMonthlyObligations;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGovId() { return govId; }
    public double getMonthlyIncome() { return monthlyIncome; }
    public int getCreditScore() { return creditScore; }
    public double getExistingMonthlyObligations() { return existingMonthlyObligations; }
}

// 2. Loan Application Model
class LoanApplication {
    private final String applicationId;
    private final Customer customer;
    private final double requestedAmount;

    public LoanApplication(String applicationId, Customer customer, double requestedAmount) {
        if (applicationId == null || applicationId.trim().isEmpty()) throw new InvalidApplicationException("Application ID cannot be empty.");
        if (customer == null) throw new InvalidApplicationException("Customer record cannot be null.");
        if (requestedAmount <= 0) throw new InvalidApplicationException("Requested loan amount must be greater than zero.");

        this.applicationId = applicationId;
        this.customer = customer;
        this.requestedAmount = requestedAmount;
    }

    public String getApplicationId() { return applicationId; }
    public Customer getCustomer() { return customer; }
    public double getRequestedAmount() { return requestedAmount; }
}

// 3. Business Logic Risk Matrix & Assessment Engine
class CreditAssessment {
    public enum RiskStatus { LOW_RISK, MEDIUM_RISK, HIGH_RISK_REJECTED }

    private final RiskStatus status;
    private final double maxPermissibleLoan;
    private final List<String> rejectionReasons;

    public CreditAssessment(RiskStatus status, double maxPermissibleLoan, List<String> rejectionReasons) {
        this.status = status;
        this.maxPermissibleLoan = maxPermissibleLoan;
        this.rejectionReasons = new ArrayList<String>(rejectionReasons); // FIXED TYPO HERE
    }

    public RiskStatus getStatus() { return status; }
    public double getMaxPermissibleLoan() { return maxPermissibleLoan; }
    public List<String> getRejectionReasons() { return rejectionReasons; }

    @Override
    public String toString() {
        return "Status: " + status + " | Max Permissible Limit: " + maxPermissibleLoan + 
               (rejectionReasons.isEmpty() ? "" : " | Rejection Flaws: " + rejectionReasons);
    }

    public static CreditAssessment assessLoan(LoanApplication application) {
        if (application == null) throw new InvalidApplicationException("Application cannot be null.");
        
        Customer customer = application.getCustomer();
        List<String> reasons = new ArrayList<String>();

        // Condition A: Basic Gate-keeping Criteria 
        if (customer.getAge() < 21) {
            reasons.add("Customer age is below 21.");
        }
        if (customer.getMonthlyIncome() < 25000.0) {
            reasons.add("Monthly income fails minimum threshold criteria of 25,000.");
        }
        if (customer.getCreditScore() < 600) {
            reasons.add("Credit score below minimum baseline requirement of 600.");
        }

        // Condition B: Debt-to-Income (DTI) Calculations
        double existingObligations = customer.getExistingMonthlyObligations();
        double dtiRatio = (customer.getMonthlyIncome() > 0) ? (existingObligations / customer.getMonthlyIncome()) : 1.0;
        
        if (dtiRatio > 0.50) {
            reasons.add("Debt-to-Income ratio exceeds maximum limit of 50%. Actual DTI: " + String.format("%.2f", dtiRatio * 100) + "%");
        }

        // Condition C: Income-Dependent Max Loan Multiplier Tier Allocation
        double multiplier = 0.0;
        if (customer.getCreditScore() >= 750) {
            multiplier = 15.0; 
        } else if (customer.getCreditScore() >= 650) {
            multiplier = 10.0; 
        } else if (customer.getCreditScore() >= 600) {
            multiplier = 5.0;  
        }
        
        double maxPermissibleLoan = customer.getMonthlyIncome() * multiplier;

        // Condition D: Requested Amount Boundaries Evaluation
        if (application.getRequestedAmount() > maxPermissibleLoan) {
            reasons.add("Requested amount exceeds the income-dependent maximum limit of " + maxPermissibleLoan);
        }

        // Condition E: Final Risk Routing Assignment
        if (!reasons.isEmpty()) {
            return new CreditAssessment(RiskStatus.HIGH_RISK_REJECTED, maxPermissibleLoan, reasons);
        }

        // Tiering between Safe and Moderate risk classification structures
        if (customer.getCreditScore() >= 750 && dtiRatio <= 0.30) {
            return new CreditAssessment(RiskStatus.LOW_RISK, maxPermissibleLoan, reasons);
        } else {
            return new CreditAssessment(RiskStatus.MEDIUM_RISK, maxPermissibleLoan, reasons);
        }
    }
}

public class LoanSystem {
    public static void main(String[] args) {
        System.out.println("Smart Loan Approval System Initialised successfully.");
    }
}
