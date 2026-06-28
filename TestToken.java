import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TestToken {
    public static void main(String[] args) throws Exception {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJyaXZldC1hcGkiLCJzdWIiOiJ0ZXN0dXNlcjk5OTNAZXhhbXBsZS5jb20iLCJ1c2VySWQiOjQsInJvbGUiOiJVU0VSIiwiaWF0IjoxNzgyNjg1NTI2LCJleHAiOjE3ODI2ODkxMjZ9.Xn6Wck7tCKdKLtgxV2lTcDmDejtWPROIS4EwrFxyUXo";
        String[] parts = token.split("\\.");
        String unsignedToken = parts[0] + "." + parts[1];
        String secret = "dev-only-change-this-secret-before-production-1234567890";
        
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        
        System.out.println("Expected: " + parts[2]);
        System.out.println("Actual:   " + signature);
        System.out.println("Match:    " + parts[2].equals(signature));
    }
}
