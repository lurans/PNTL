package com.huawei.blackhole.network.extention.bean.vrm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Vms implements Serializable {
    private static final long serialVersionUID = -5205978481305686611L;

    private int total;

    private List<Vm> vms;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Vm> getVms() {
        return vms;
    }

    public void setVms(List<Vm> vms) {
        this.vms = vms;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    public static class Vm implements Serializable {
        private static final long serialVersionUID = -3928175435373354201L;

        private String name;

        private String group;

        private String location;

        private String status;

        private String hostUrn;

        private String clusterUrn;

        private String uri;

        private String urn;

        private String cdRomStatus;

        private String createTime;

        private boolean isLinkClone;

        private boolean isTemplate;

        private String locationName;

        private String pvDriverStatus;

        private String toolInstallStatus;

        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getHostUrn() {
            return hostUrn;
        }

        public void setHostUrn(String hostUrn) {
            this.hostUrn = hostUrn;
        }

        public String getClusterUrn() {
            return clusterUrn;
        }

        public void setClusterUrn(String clusterUrn) {
            this.clusterUrn = clusterUrn;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getUrn() {
            return urn;
        }

        public void setUrn(String urn) {
            this.urn = urn;
        }

        public String getCdRomStatus() {
            return cdRomStatus;
        }

        public void setCdRomStatus(String cdRomStatus) {
            this.cdRomStatus = cdRomStatus;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public boolean isLinkClone() {
            return isLinkClone;
        }

        public void setLinkClone(boolean isLinkClone) {
            this.isLinkClone = isLinkClone;
        }

        public boolean isTemplate() {
            return isTemplate;
        }

        public void setTemplate(boolean isTemplate) {
            this.isTemplate = isTemplate;
        }

        public String getLocationName() {
            return locationName;
        }

        public void setLocationName(String locationName) {
            this.locationName = locationName;
        }

        public String getPvDriverStatus() {
            return pvDriverStatus;
        }

        public void setPvDriverStatus(String pvDriverStatus) {
            this.pvDriverStatus = pvDriverStatus;
        }

        public String getToolInstallStatus() {
            return toolInstallStatus;
        }

        public void setToolInstallStatus(String toolInstallStatus) {
            this.toolInstallStatus = toolInstallStatus;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    public static class VmInfo extends Vm {

        /**
         *
         */
        private static final long serialVersionUID = -5140711123484365002L;

        @JsonProperty("vmConfig")
        private VmConfig vmConfig;

        @JsonProperty("vncAcessInfo")
        private VncAcessInfo vncAcessInfo;


        public VncAcessInfo getVncAcessInfo() {
            return vncAcessInfo;
        }

        public void setVncAcessInfo(VncAcessInfo vncAcessInfo) {
            this.vncAcessInfo = vncAcessInfo;
        }

        public VmConfig getVmConfig() {
            return vmConfig;
        }

        public void setVmConfig(VmConfig vmConfig) {
            this.vmConfig = vmConfig;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(Include.NON_NULL)
        public static class VncAcessInfo implements Serializable {
            private static final long serialVersionUID = 3888490694863983713L;
            @JsonProperty("hostIp")
            private String hostIp;

            @JsonProperty("vncPassword")
            private String vncPassword;

            @JsonProperty("vncPort")
            private String vncPort;

            public String getHostIp() {
                return hostIp;
            }

            public void setHostIp(String hostIp) {
                this.hostIp = hostIp;
            }

            public String getVncPassword() {
                return vncPassword;
            }

            public void setVncPassword(String vncPassword) {
                this.vncPassword = vncPassword;
            }

            public String getVncPort() {
                return vncPort;
            }

            public void setVncPort(String vncPort) {
                this.vncPort = vncPort;
            }

            @Override
            public String toString() {
                return "VncAcessInfo [hostIp=" + hostIp + ", vncPassword="
                        + vncPassword + ", vncPort=" + vncPort + "]";
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(Include.NON_NULL)
        public static class VmConfig implements Serializable {
            private static final long serialVersionUID = -4522284556668335486L;

            @JsonProperty("cpu")
            private CPU cpu;

            @JsonProperty("disks")
            private List<Disk> disks;

            @JsonProperty("memory")
            private Memory memory;

            @JsonProperty("nics")
            private List<Nic> nics;

            public CPU getCpu() {
                return cpu;
            }

            public void setCpu(CPU cpu) {
                this.cpu = cpu;
            }

            public List<Disk> getDisks() {
                return disks;
            }

            public void setDisks(List<Disk> disks) {
                this.disks = disks;
            }

            public Memory getMemory() {
                return memory;
            }

            public void setMemory(Memory memory) {
                this.memory = memory;
            }

            public List<Nic> getNics() {
                return nics;
            }

            public void setNics(List<Nic> nics) {
                this.nics = nics;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonInclude(Include.NON_NULL)
            public static class CPU implements Serializable {

                /**
                 *
                 */
                private static final long serialVersionUID = 2880677838005586412L;

            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonInclude(Include.NON_NULL)
            public static class Disk implements Serializable {

                /**
                 *
                 */
                private static final long serialVersionUID = -8302681640226266409L;

            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonInclude(Include.NON_NULL)
            public static class Memory implements Serializable {

                /**
                 *
                 */
                private static final long serialVersionUID = 8007393543224375866L;

            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonInclude(Include.NON_NULL)
            public static class Nic implements Serializable {

                /**
                 *
                 */
                private static final long serialVersionUID = 7464725991024535811L;

                @JsonProperty("ip")
                private String ip;

                @JsonProperty("mac")
                private String mac;

                @JsonProperty("name")
                private String name;

                public String getIp() {
                    return ip;
                }

                public void setIp(String ip) {
                    this.ip = ip;
                }

                public String getMac() {
                    return mac;
                }

                public void setMac(String mac) {
                    this.mac = mac;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

            }

        }


    }


}
