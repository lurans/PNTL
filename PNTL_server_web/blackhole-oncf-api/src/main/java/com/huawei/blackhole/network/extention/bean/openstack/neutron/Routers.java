package com.huawei.blackhole.network.extention.bean.openstack.neutron;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Routers implements Serializable {
    private static final long serialVersionUID = 4153736321892931256L;

    @JsonProperty("routers")
    private List<Router> list;

    public List<Router> getList() {
        return list;
    }

    public void setList(List<Router> list) {
        this.list = list;
    }

    public String toString() {
        return "Routers [list=" + list + "]";
    }

    public Iterator<Router> iterator() {
        return list.iterator();
    }

    @JsonRootName("router")
    @JsonInclude(Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public final static class Router implements Serializable {
        private static final long serialVersionUID = -327746757069479289L;

        private String id;

        private String name;

        @JsonProperty("subnet_id")
        private String subnetID;

        @JsonProperty("tenant_id")
        private String tenantId;

        private String status;

        @JsonProperty("admin_state_up")
        private boolean adminStateUp;

        @JsonProperty("external_gateway_info")
        private ExternalGWInfo externalGWInfo;

        @JsonProperty("routes")
        private List<HostRoute> routeList;

        private boolean ha;

        public boolean isHa() {
            return ha;
        }

        public void setHa(boolean ha) {
            this.ha = ha;
        }

        /**
         * @return the tenantId
         */
        public String getTenantId() {
            return tenantId;
        }

        /**
         * @param tenantId
         *            the tenantId to set
         */
        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @param id
         *            the id to set
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name
         *            the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the adminStateUp
         */
        public boolean isAdminStateUp() {
            return adminStateUp;
        }

        /**
         * @param adminStateUp
         *            the adminStateUp to set
         */
        public void setAdminStateUp(boolean adminStateUp) {
            this.adminStateUp = adminStateUp;
        }

        /**
         * @return the status
         */
        public String getStatus() {
            return status;
        }

        /**
         * @param status
         *            the status to set
         */
        public void setStatus(String status) {
            this.status = status;
        }

        /**
         * @return the externalGWInfo
         */
        public ExternalGWInfo getExternalGWInfo() {
            return externalGWInfo;
        }

        /**
         * @param externalGWInfo
         *            the externalGWInfo to set
         */
        public void setExternalGWInfo(ExternalGWInfo externalGWInfo) {
            this.externalGWInfo = externalGWInfo;
        }

        /**
         * 返回subnetID
         *
         * @return 返回subnetID
         */
        public String getSubnetID() {
            return subnetID;
        }

        /**
         * 设置subnetID
         *
         * @param subnetID
         *            要设置的subnetID
         */
        public void setSubnetID(String subnetID) {
            this.subnetID = subnetID;
        }

        /**
         * 返回routeList
         *
         * @return 返回routeList
         */
        public List<HostRoute> getRouteList() {
            return routeList;
        }

        /**
         * 设置routeList
         *
         * @param routeList
         *            要设置的routeList
         */
        public void setRouteList(List<HostRoute> routeList) {
            this.routeList = routeList;
        }

        @Override
        public String toString() {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("Router [name = ");
            strBuilder.append(name);
            strBuilder.append(", id=");
            strBuilder.append(id);
            strBuilder.append(", adminStateUp=");
            strBuilder.append(adminStateUp);
            strBuilder.append(", status=");
            strBuilder.append(status);
            strBuilder.append(", subnetID=");
            strBuilder.append(subnetID);
            strBuilder.append(", tenantId=");
            strBuilder.append(tenantId);
            strBuilder.append(", externalGWInfo=");
            strBuilder.append(externalGWInfo);
            strBuilder.append(", routeList=");
            strBuilder.append(routeList);
            strBuilder.append("]");
            return strBuilder.toString();
        }

        /**
         * 主机路由信息对象
         */
        @JsonInclude(Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static final class HostRoute implements Serializable {
            /**
             *
             */
            private static final long serialVersionUID = 8517722869345918815L;

            /**
             * 目的地址子网IP，格式为xxx.xxx.xxx.xxx/24
             */
            private String destination;

            /**
             * 下一跳IP，如：xxx.xxx.xxx.xxxx
             */
            private String nexthop;

            public HostRoute() {
                super();
            }

            public HostRoute(String destination, String nexthop) {
                super();
                this.destination = destination;
                this.nexthop = nexthop;
            }

            public String getDestination() {
                return destination;
            }

            public void setDestination(String destination) {
                this.destination = destination;
            }

            public String getNexthop() {
                return nexthop;
            }

            public void setNexthop(String nexthop) {
                this.nexthop = nexthop;
            }

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("HostRoute [destination=");
                builder.append(destination);
                builder.append(", nexthop=");
                builder.append(nexthop);
                builder.append("]");
                return builder.toString();
            }

        }

        @JsonInclude(Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static final class ExternalGWInfo implements Serializable {

            /**
             *
             */
            private static final long serialVersionUID = 7122142207624244410L;

            @JsonProperty("network_id")
            private String networkID;

            @JsonProperty("enable_snat")
            private boolean enableSnat;

            public ExternalGWInfo() {
                super();
            }

            public ExternalGWInfo(String networkID, boolean enableSnat) {
                super();
                this.networkID = networkID;
                this.enableSnat = enableSnat;
            }

            public String getNetworkID() {
                return networkID;
            }

            public void setNetworkID(String networkID) {
                this.networkID = networkID;
            }

            public boolean isEnableSnat() {
                return enableSnat;
            }

            public void setEnableSnat(boolean enableSnat) {
                this.enableSnat = enableSnat;
            }

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("ExternalGWInfo [networkID=");
                builder.append(networkID);
                builder.append(", enableSnat=");
                builder.append(enableSnat);
                builder.append("]");
                return builder.toString();
            }

        }
    }

}
