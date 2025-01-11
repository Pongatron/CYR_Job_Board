package DatabaseInteraction;


public class Filter {
    private String filterName;
    private FilterStatus filterStatus;
    public enum FilterStatus {NONE, ASC, DESC}

    public Filter(String name, FilterStatus status){
        filterName = name;
        filterStatus = status;

    }

    public void setFilterStatus(FilterStatus filterStatus) {
        this.filterStatus = filterStatus;
    }

    public FilterStatus getFilterStatus() {
        return filterStatus;
    }

    public String getFilterName() {
        return filterName;
    }
}
