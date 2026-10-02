import java.net.InetAddress;

public class Test {

    public static void main(String[] args)
            throws Exception {

        InetAddress address =
            InetAddress.getByName("8.8.8.8");

        System.out.println(
            "HostName: "
            + address.getHostName());

        System.out.println(
            "Canonical: "
            + address.getCanonicalHostName());
    }
}
