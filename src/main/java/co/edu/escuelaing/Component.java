package co.edu.escuelaing;


/**
 * Esta clase encapsula información básica que en nuestro caso representa un componente que puede
 * ser libro, película o serie y que tiene un nombre, su tipo, una descripción y una calificación.
 * Además se incluten los getters por cada atributo.
 */
class Component {
    private String name;
    private String type;
    private String description;
    private int rating;

    public Component(String name, String type, String description, int rating) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.rating = rating;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public int getRating() { return rating; }

    @Override
    public String toString() {
        return "{\"name\":\"" + name + "\", \"type\":\"" + type + 
               "\", \"description\":\"" + description + "\", \"rating\":" + rating + "}";
    }
}