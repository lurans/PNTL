package com.huawei.blackhole.network.dt;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.cas.client.proxy.AbstractEncryptedProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.proxy.Cas20ProxyRetriever;
import org.jasig.cas.client.proxy.CleanUpTimerTask;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.ssl.HttpURLConnectionFactory;
import org.jasig.cas.client.ssl.HttpsURLConnectionFactory;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.ReflectUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cas20ProxyReceivingTicketValidationFilter extends AbstractTicketValidationFilter {

    private static final Logger OPERATE_LOG = LoggerFactory.getLogger("console_operation_log");
    private static final Logger logger = LoggerFactory.getLogger(Cas20ProxyReceivingTicketValidationFilter.class);

    private static final String[] RESERVED_INIT_PARAMS = new String[] { "proxyGrantingTicketStorageClass",
            "proxyReceptorUrl", "acceptAnyProxy", "allowedProxyChains", "casServerUrlPrefix", "proxyCallbackUrl",
            "renew", "exceptionOnValidationFailure", "redirectAfterValidation", "useSession", "serverName", "service",
            "artifactParameterName", "serviceParameterName", "encodeServiceUrl", "millisBetweenCleanUps",
            "hostnameVerifier", "encoding", "config", "ticketValidatorClass" };

    private static final int DEFAULT_MILLIS_BETWEEN_CLEANUPS = 60 * 1000;

    /**
     * The URL to send to the CAS server as the URL that will process proxying
     * requests on the CAS client.
     */
    private String proxyReceptorUrl;

    private Timer timer;

    private TimerTask timerTask;

    private int millisBetweenCleanUps;

    private String casServerUrlPrefix;

    private HttpURLConnectionFactory urlConnfactory;

    private String validatorEncoding;

    /**
     * Storage location of ProxyGrantingTickets and Proxy Ticket IOUs.
     */
    private ProxyGrantingTicketStorage proxyGrantingTicketStorage = new ProxyGrantingTicketStorageImpl();

    protected void initInternal(final FilterConfig filterConfig) throws ServletException {
        casServerUrlPrefix = getPropertyFromInitParams(filterConfig, "casServerUrlPrefix", null);

        setProxyReceptorUrl(getPropertyFromInitParams(filterConfig, "proxyReceptorUrl", null));

        final String proxyGrantingTicketStorageClass = getPropertyFromInitParams(filterConfig,
                "proxyGrantingTicketStorageClass", null);

        if (proxyGrantingTicketStorageClass != null) {
            this.proxyGrantingTicketStorage = ReflectUtils.newInstance(proxyGrantingTicketStorageClass);

            if (this.proxyGrantingTicketStorage instanceof AbstractEncryptedProxyGrantingTicketStorageImpl) {
                final AbstractEncryptedProxyGrantingTicketStorageImpl p = (AbstractEncryptedProxyGrantingTicketStorageImpl) this.proxyGrantingTicketStorage;
                final String cipherAlgorithm = getPropertyFromInitParams(filterConfig, "cipherAlgorithm",
                        AbstractEncryptedProxyGrantingTicketStorageImpl.DEFAULT_ENCRYPTION_ALGORITHM);
                final String secretKey = getPropertyFromInitParams(filterConfig, "secretKey", null);

                p.setCipherAlgorithm(cipherAlgorithm);

                try {
                    if (secretKey != null) {
                        p.setSecretKey(secretKey);
                    }
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        logger.trace("Setting proxyReceptorUrl parameter: {}", this.proxyReceptorUrl);
        this.millisBetweenCleanUps = Integer.parseInt(getPropertyFromInitParams(filterConfig, "millisBetweenCleanUps",
                Integer.toString(DEFAULT_MILLIS_BETWEEN_CLEANUPS)));
        super.initInternal(filterConfig);
    }

    public void init() {
        super.init();
        CommonUtils.assertNotNull(this.proxyGrantingTicketStorage, "proxyGrantingTicketStorage cannot be null.");

        if (this.timer == null) {
            this.timer = new Timer(true);
        }

        if (this.timerTask == null) {
            this.timerTask = new CleanUpTimerTask(this.proxyGrantingTicketStorage);
        }
        this.timer.schedule(this.timerTask, this.millisBetweenCleanUps, this.millisBetweenCleanUps);
    }

    private <T> T createNewTicketValidator(final String ticketValidatorClass, final String casServerUrlPrefix,
            final Class<T> clazz) {
        if (CommonUtils.isBlank(ticketValidatorClass)) {
            return ReflectUtils.newInstance(clazz, casServerUrlPrefix);
        }

        return ReflectUtils.newInstance(ticketValidatorClass, casServerUrlPrefix);
    }

    /**
     * Constructs a Cas20ServiceTicketValidator or a Cas20ProxyTicketValidator
     * based on supplied parameters.
     *
     * @param filterConfig
     *            the Filter Configuration object.
     * @return a fully constructed TicketValidator.
     */
    protected final TicketValidator getTicketValidator(final FilterConfig filterConfig) {
        final String allowAnyProxy = getPropertyFromInitParams(filterConfig, "acceptAnyProxy", null);
        final String allowedProxyChains = getPropertyFromInitParams(filterConfig, "allowedProxyChains", null);
        final String casServerUrlPrefix = getPropertyFromInitParams(filterConfig, "casServerUrlPrefix", null);
        final String ticketValidatorClass = getPropertyFromInitParams(filterConfig, "ticketValidatorClass", null);
        final Cas20ServiceTicketValidator validator;

        if (CommonUtils.isNotBlank(allowAnyProxy) || CommonUtils.isNotBlank(allowedProxyChains)) {
            final Cas20ProxyTicketValidator v = createNewTicketValidator(ticketValidatorClass, casServerUrlPrefix,
                    Cas20ProxyTicketValidator.class);
            v.setAcceptAnyProxy(parseBoolean(allowAnyProxy));
            v.setAllowedProxyChains(CommonUtils.createProxyList(allowedProxyChains));
            validator = v;
        } else {
            validator = createNewTicketValidator(ticketValidatorClass, casServerUrlPrefix,
                    Cas20ServiceTicketValidator.class);
        }
        validator.setProxyCallbackUrl(getPropertyFromInitParams(filterConfig, "proxyCallbackUrl", null));
        validator.setProxyGrantingTicketStorage(this.proxyGrantingTicketStorage);

        final HttpURLConnectionFactory factory = new HttpsURLConnectionFactory(getHostnameVerifier(filterConfig),
                getSSLConfig(filterConfig));
        validator.setURLConnectionFactory(factory);
        this.urlConnfactory = factory;

        validator.setProxyRetriever(new Cas20ProxyRetriever(casServerUrlPrefix, getPropertyFromInitParams(filterConfig,
                "encoding", null), factory));
        validator.setRenew(parseBoolean(getPropertyFromInitParams(filterConfig, "renew", "false")));
        this.validatorEncoding = getPropertyFromInitParams(filterConfig, "encoding", null);
        validator.setEncoding(this.validatorEncoding);

        final Map<String, String> additionalParameters = new HashMap<String, String>();
        final List<String> params = Arrays.asList(RESERVED_INIT_PARAMS);

        for (final Enumeration<?> e = filterConfig.getInitParameterNames(); e.hasMoreElements();) {
            final String s = (String) e.nextElement();

            if (!params.contains(s)) {
                additionalParameters.put(s, filterConfig.getInitParameter(s));
            }
        }

        validator.setCustomParameters(additionalParameters);
        return validator;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (!preFilter(servletRequest, servletResponse, filterChain)) {
            return;
        }

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        tgtRenewal(request); // 更新TGT
        final String ticket = retrieveTicketFromRequest(request);

        if (CommonUtils.isNotBlank(ticket)) {
            logger.debug("Attempting to validate ticket: {}", ticket);

            try {
                final Assertion assertion = this.ticketValidator.validate(ticket,
                        constructServiceUrl(request, response));

                String user = assertion.getPrincipal().getName();
                logger.debug("Successfully authenticated user: {}", user);
                request.getSession().setAttribute("userName", user);

                request.setAttribute(CONST_CAS_ASSERTION, assertion);

                if (this.useSession) {
                    request.getSession().setAttribute(CONST_CAS_ASSERTION, assertion);
                }
                onSuccessfulValidation(request, response, assertion);

                if (this.redirectAfterValidation) {
                    logger.debug("Redirecting after successful ticket validation.");
                    response.sendRedirect(constructServiceUrl(request, response));
                    return;
                }
            } catch (final TicketValidationException e) {
                logger.debug(e.getMessage(), e);

                onFailedValidation(request, response);

                if (this.exceptionOnValidationFailure) {
                    throw new ServletException(e);
                }

                response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());

                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // 更新TGT
    private void tgtRenewal(final HttpServletRequest request) throws MalformedURLException {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            logger.error("No session currently exists (and none created).  Cannot record session information for single sign out.");
            return;
        }

        // 获取上次续期时间
        Long preTime = SingleSignOutHandler.sessionMappingStorage.getAccessTimeBySessionId(session);
        // 获取Ticket
        String ticket = SingleSignOutHandler.sessionMappingStorage.getMappingIdBySessionId(session);
        if (preTime == null || ticket == null) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        long invertal = 1 * 60 * 1000; // 续期间隔 1分钟
        if (currentTime - preTime.longValue() >= invertal) {
            // 发送续期请求 接口 参照9.4
            final int statusCode = this.getStatusCode(ticket);
            if (statusCode == 200) // 续期成功
            {
                // 更新TGT续期时间
                SingleSignOutHandler.sessionMappingStorage.updateAccessTimeBySessionId(session);
            }
        }
    }

    private int getStatusCode(String ticket) throws MalformedURLException {
        String tgtUrl = constructTGTUrl(ticket);
        int statusCode = getStatusCodeFromServer(new URL(tgtUrl), this.urlConnfactory, this.validatorEncoding);
        OPERATE_LOG.info("[TGT REQUEST]: GET {}, responseCode={}", tgtUrl, statusCode);
        return statusCode;
    }

    private String constructTGTUrl(String ticket) {
        StringBuilder sb = new StringBuilder(casServerUrlPrefix);
        if (!this.casServerUrlPrefix.endsWith("/")) {
            sb.append("/");
        }
        sb.append("touch?ticket=");
        sb.append(ticket);
        return sb.toString();
    }

    public int getStatusCodeFromServer(final URL constructedUrl, final HttpURLConnectionFactory factory,
            final String encoding) {

        HttpURLConnection conn = null;
        InputStreamReader in = null;
        try {
            conn = factory.buildHttpURLConnection(constructedUrl.openConnection());
            conn.setRequestMethod("GET");

            conn.connect();
            return conn.getResponseCode();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeQuietly(in);
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static void closeQuietly(final Closeable resource) {
        try {
            if (resource != null) {
                resource.close();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        super.destroy();
        this.timer.cancel();
    }

    /**
     * This processes the ProxyReceptor request before the ticket validation
     * code executes.
     */
    protected final boolean preFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final String requestUri = request.getRequestURI();

        if (CommonUtils.isEmpty(this.proxyReceptorUrl) || !requestUri.endsWith(this.proxyReceptorUrl)) {
            return true;
        }

        try {
            CommonUtils.readAndRespondToProxyReceptorRequest(request, response, this.proxyGrantingTicketStorage);
        } catch (final RuntimeException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }

        return false;
    }

    protected void onSuccessfulValidation(final HttpServletRequest request, final HttpServletResponse response,
            final Assertion assertion) {
        String reqUrl = request.getRequestURL().toString();
        String reqMethod = request.getMethod();
        String userName = assertion.getPrincipal().getName();
        OPERATE_LOG.info(String.format("[sso login] %s %s with username %s return response: %s", reqMethod, reqUrl,
                userName, response.getStatus()));
    }

    protected void onFailedValidation(final HttpServletRequest request, final HttpServletResponse response) {
        String reqUrl = request.getRequestURL().toString();
        String reqMethod = request.getMethod();
        OPERATE_LOG.info(String.format("[sso login] %s %s with username return response: %s", reqMethod, reqUrl,
                response.getStatus()));
    }

    public final void setProxyReceptorUrl(final String proxyReceptorUrl) {
        this.proxyReceptorUrl = proxyReceptorUrl;
    }

    public void setProxyGrantingTicketStorage(final ProxyGrantingTicketStorage storage) {
        this.proxyGrantingTicketStorage = storage;
    }

    public void setTimer(final Timer timer) {
        this.timer = timer;
    }

    public void setTimerTask(final TimerTask timerTask) {
        this.timerTask = timerTask;
    }

    public void setMillisBetweenCleanUps(final int millisBetweenCleanUps) {
        this.millisBetweenCleanUps = millisBetweenCleanUps;
    }

}