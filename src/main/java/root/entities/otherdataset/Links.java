package root.entities.otherdataset;

import com.google.gson.annotations.SerializedName;

public class Links {
    @SerializedName("collection")
    private Collection collection;
    @SerializedName("next")
    private Collection next;
    @SerializedName("parent")
    private Collection parent;
    @SerializedName("self")
    private Collection self;

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public Collection getNext() {
        return next;
    }

    public void setNext(Collection next) {
        this.next = next;
    }

    public Collection getParent() {
        return parent;
    }

    public void setParent(Collection parent) {
        this.parent = parent;
    }

    public Collection getSelf() {
        return self;
    }

    public void setSelf(Collection self) {
        this.self = self;
    }
}
