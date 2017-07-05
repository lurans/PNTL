package com.huawei.blackhole.network.extention.bean.pntl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.io.Serializable;
import java.util.List;

public class PntlNetworkMap implements Serializable {
    private static final long serialVersionUID = -7466223934978978053L;

    private String azName;

    private List<CoreSwitchInfo> coreList;

    public String getAzName() {
        return azName;
    }

    public void setAzName(String azName) {
        this.azName = azName;
    }

    public List<CoreSwitchInfo> getCoreList() {
        return coreList;
    }

    public void setCoreList(List<CoreSwitchInfo> coreList) {
        this.coreList = coreList;
    }

    public class CoreSwitchInfo extends PntlNetworkMap implements Serializable{

        private static final long serialVersionUID = -5745695428221346073L;

        private AggSwitch aggSwitch;

        private CoreSwitch coreSwitch;

        private List<Pod> podList;

        public AggSwitch getAggSwitch() {
            return aggSwitch;
        }

        public void setAggSwitch(AggSwitch aggSwitch) {
            this.aggSwitch = aggSwitch;
        }

        public CoreSwitch getCoreSwitch() {
            return coreSwitch;
        }

        public void setCoreSwitch(CoreSwitch coreSwitch) {
            this.coreSwitch = coreSwitch;
        }

        public List<Pod> getPodList() {
            return podList;
        }

        public void setPodList(List<Pod> podList) {
            this.podList = podList;
        }

        public class IpList implements Serializable{

            private static final long serialVersionUID = -4992637208439168187L;

            private String ip;

            public String getIp() {
                return ip;
            }

            public void setIp(String ip) {
                this.ip = ip;
            }
        }

        public class AggSwitch extends PntlNetworkMap implements Serializable{

            private static final long serialVersionUID = -5250685569305659922L;

            private List<IpList> ipList;

            public List<IpList> getIpList() {
                return ipList;
            }

            public void setIpList(List<IpList> ipList) {
                this.ipList = ipList;
            }
        }

        public class CoreSwitch extends  PntlNetworkMap implements Serializable{

            private static final long serialVersionUID = 5358292937584742377L;

            private List<IpList> ipList;

            public List<IpList> getIpList() {
                return ipList;
            }

            public void setIpList(List<IpList> ipList) {
                this.ipList = ipList;
            }
        }

        public class Pod extends CoreSwitchInfo implements Serializable{

            private static final long serialVersionUID = 6849000263221315575L;

            private String podName;

            private String podId;

            private List<Server> serverList;

            public String getPodName() {
                return podName;
            }

            public void setPodName(String podName) {
                this.podName = podName;
            }

            public String getPodId() {
                return podId;
            }

            public void setPodId(String podId) {
                this.podId = podId;
            }

            public List<Server> getServerList() {
                return serverList;
            }

            public void setServerList(List<Server> serverList) {
                this.serverList = serverList;
            }

            public class Server extends PntlNetworkMap implements Serializable{

                private static final long serialVersionUID = -1949505766204936607L;

                public String ip;

                public String sn;

                public String getIp() {
                    return ip;
                }

                public void setIp(String ip) {
                    this.ip = ip;
                }

                public String getSn() {
                    return sn;
                }

                public void setSn(String sn) {
                    this.sn = sn;
                }
            }
        }
    }

}
