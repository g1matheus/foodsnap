package ai.fooz.foodanalysis;

public class FoodItem {

    private String name, image;
    private Long id;

    public FoodItem() {
    }

    public FoodItem(Long id, String name, String image) {
        this.name = name;
        this.image = image;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
