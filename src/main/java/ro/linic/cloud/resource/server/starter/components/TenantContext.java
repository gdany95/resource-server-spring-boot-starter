package ro.linic.cloud.resource.server.starter.components;

public class TenantContext {
    private static final ThreadLocal<Integer> CURRENT_TENANT = new ThreadLocal<>();

    public static Integer getCurrentTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void setCurrentTenantId(final Integer tenantId) {
        CURRENT_TENANT.set(tenantId);
    }
}