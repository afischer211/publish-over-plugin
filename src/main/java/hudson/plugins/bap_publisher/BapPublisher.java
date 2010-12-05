package hudson.plugins.bap_publisher;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BapPublisher<TRANSFER extends BPTransfer> implements Serializable {

    static final long serialVersionUID = 1L;
    
    private String configName;
    private boolean verbose;
	private List<TRANSFER> transfers;

    public BapPublisher() {}

	public BapPublisher(String configName, boolean verbose, List<TRANSFER> transfers) {
		this.configName = configName;
        this.verbose = verbose;
		setTransfers(transfers);
	}

    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }

    public boolean isVerbose() { return verbose; }
    public void setVerbose(boolean verbose) { this.verbose = verbose; }

    public List<TRANSFER> getTransfers() {
        return transfers;
    }
    public void setTransfers(List<TRANSFER> transfers) {
        if (transfers == null) {
            this.transfers = new ArrayList<TRANSFER>();
        } else {
            this.transfers = transfers;
        }
    }

    private int sumTransfers(List<Integer> transferred) {
        int total = 0;
        for (int tx : transferred) {
            total += tx;
        }
        return total;
    }

    private void printNumberOfFilesTransferred(BPBuildInfo buildInfo, List<Integer> transferred) {
        int total = sumTransfers(transferred);
        String countString = "" + total;
        if (transferred.size() > 1) {
            countString = total + " ( " + StringUtils.join(transferred, " + ") + " )";
        }
        buildInfo.println(Messages.console_transferredXFiles(countString));
    }

    public void perform(BPHostConfiguration hostConfig, BPBuildInfo buildInfo) throws Exception {
        buildInfo.setVerbose(verbose);
        buildInfo.println(Messages.console_connecting(configName));
        BPClient client = hostConfig.createClient(buildInfo);
        List<Integer> transferred = new ArrayList<Integer>();
        try {
            for (TRANSFER transfer : transfers) {
                client.beginTransfers(transfer);
                transferred.add(transfer.transfer(buildInfo, client));
                client.endTransfers(transfer);
            }
            printNumberOfFilesTransferred(buildInfo, transferred);
        } finally {
            buildInfo.println(Messages.console_disconnecting(configName));
            client.disconnectQuietly();
        }
	}
    
    protected HashCodeBuilder createHashCodeBuilder() {
        return addToHashCode(new HashCodeBuilder());
    }
    
    protected HashCodeBuilder addToHashCode(HashCodeBuilder builder) {
        return builder.append(configName).append(verbose).append(transfers);
    }
    
    protected EqualsBuilder createEqualsBuilder(BapPublisher that) {
        return addToEquals(new EqualsBuilder(), that);
    }
    
    protected EqualsBuilder addToEquals(EqualsBuilder builder, BapPublisher that) {
        return builder.append(configName, that.configName)
            .append(verbose, that.verbose)
            .append(transfers, that.transfers);
    }
    
    protected ToStringBuilder addToToString(ToStringBuilder builder) {
        return builder.append("configName", configName)
            .append("verbose", verbose)
            .append("transfers", transfers);
    }
    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        return createEqualsBuilder((BapPublisher) o).isEquals();
    }

    public int hashCode() {
        return createHashCodeBuilder().toHashCode();
    }
    
    public String toString() {
        return addToToString(new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)).toString();
    }

}