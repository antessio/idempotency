package antessio.idempotency;

import java.util.Objects;

public class StubTarget {

    private String field1;
    private Integer field2;
    private Boolean field3;

    public StubTarget(String field1, Integer field2, Boolean field3) {
        this.field1 = field1;
        this.field2 = field2;
        this.field3 = field3;
    }

    public String getField1() {
        return field1;
    }

    public Integer getField2() {
        return field2;
    }

    public Boolean getField3() {
        return field3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StubTarget target = (StubTarget) o;

        if (!Objects.equals(field1, target.field1)) {
            return false;
        }
        if (!Objects.equals(field2, target.field2)) {
            return false;
        }
        return Objects.equals(field3, target.field3);
    }

    @Override
    public int hashCode() {
        int result = field1 != null ? field1.hashCode() : 0;
        result = 31 * result + (field2 != null ? field2.hashCode() : 0);
        result = 31 * result + (field3 != null ? field3.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StubTarget{" +
               "field1='" + field1 + '\'' +
               ", field2=" + field2 +
               ", field3=" + field3 +
               '}';
    }

}
