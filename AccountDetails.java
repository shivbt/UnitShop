package com.ikai.unitshop;

/**
 * Created by shiv on 22/11/17.
 */

class AccountDetails {
    public String bankName, accountHolder, accountNumber, IFSCCode, aadhaarNumber,
            UPIVPA, paytm;

    // Default constructor required for calls to DataSnapshot.getValue(Seller.class)
    public AccountDetails() {}

    public AccountDetails( final String bankName, final String accountHolder,
                           final String accountNumber, final String IFSCCode,
                           final String aadhaarNumber, final String UPIVPA, final String paytm) {
        this.bankName = bankName;
        this.accountHolder = accountHolder;
        this.accountNumber = accountNumber;
        this.IFSCCode = IFSCCode;
        this.aadhaarNumber = aadhaarNumber;
        this.UPIVPA =UPIVPA;
        this.paytm = paytm;
    }
}
