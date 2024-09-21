package com.starryassociates.trdpro.model.poc;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.Objects;

@DynamoDbBean
public class AccountSplit {

    private String accountCode;
    private double percentage;

    @DynamoDbAttribute("accountCode")
    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    @DynamoDbAttribute("percentage")
    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountSplit that = (AccountSplit) o;
        return Objects.equals(accountCode, that.accountCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountCode);
    }

    @Override
    public String toString() {
        return "AccountSplit{" +
                "accountCode='" + accountCode + '\'' +
                ", percentage=" + percentage +
                '}';
    }
}
