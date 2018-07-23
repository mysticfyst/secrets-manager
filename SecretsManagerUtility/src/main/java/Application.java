public class Application {
    public static void main(String[] args) throws Exception {
        System.out.println(new SecretsManagerUtil().getKey("dev-staging/kredx/encryptionKey","EncryptionKey"));
    }
}
