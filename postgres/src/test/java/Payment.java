import java.util.Objects;
import java.util.UUID;

public class Payment {

    private UUID id;
    private Long amountUnit;
    private String currency;
    private String sourceAccountId;

    public Payment() {
    }

    public Payment(UUID id, Long amountUnit, String currency, String sourceAccountId) {
        this.id = id;
        this.amountUnit = amountUnit;
        this.currency = currency;
        this.sourceAccountId = sourceAccountId;
    }

    public UUID getId() {
        return id;
    }

    public Long getAmountUnit() {
        return amountUnit;
    }

    public String getCurrency() {
        return currency;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Payment payment = (Payment) o;

        if (!Objects.equals(id, payment.id)) {
            return false;
        }
        if (!Objects.equals(amountUnit, payment.amountUnit)) {
            return false;
        }
        if (!Objects.equals(currency, payment.currency)) {
            return false;
        }
        return Objects.equals(sourceAccountId, payment.sourceAccountId);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (amountUnit != null ? amountUnit.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (sourceAccountId != null ? sourceAccountId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Payment{" +
               "id=" + id +
               ", amountUnit=" + amountUnit +
               ", currency='" + currency + '\'' +
               ", sourceAccountId='" + sourceAccountId + '\'' +
               '}';
    }

}
