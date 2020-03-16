package sample;

public class Item {

    // name is the unique id
    private String name;
    private boolean expires; // defaults to false

    // constructor
    public Item(String name, boolean expires) {
        this.name = name;
        this.expires = expires;
    }
    public Item(String name) {
        this.name = name;
    }
    // constructor


    public String getName() {
        return this.name;
    }

    public boolean canExpire()
    {
        return this.expires;
    }

    public String toString() {
        return  this.name;
    }
    public String todetailString() {
        return  "[ name: " + this.name
                + ", expires: " + this.expires
                + " ]";
    }
}