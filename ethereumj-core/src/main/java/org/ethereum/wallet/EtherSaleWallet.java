package org.ethereum.wallet;

//import javax.xml.bind.DatatypeConverter;

public class EtherSaleWallet {

    private String encseed;
    private String ethaddr;
    private String email;
    private String btcaddr;

    public String getEncseed() {
        return encseed;
    }

    public byte[] getEncseedBytes() {
        return hexStringToByteArray(encseed);
    }

    public void setEncseed(String encseed) {
        this.encseed = encseed;
    }

    public String getEthaddr() {
        return ethaddr;
    }

    public byte[] getEthaddrBytes() {
        return hexStringToByteArray(ethaddr);
    }

    public void setEthaddr(String ethaddr) {
        this.ethaddr = ethaddr;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBtcaddr() {
        return btcaddr;
    }

    public void setBtcaddr(String btcaddr) {
        this.btcaddr = btcaddr;
    }

    @Override
    public String toString() {
        return "EtherSaleWallet{" +
                "encseed='" + encseed + '\'' +
                ", ethaddr='" + ethaddr + '\'' +
                ", email='" + email + '\'' +
                ", btcaddr='" + btcaddr + '\'' +
                '}';
    }

	public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
