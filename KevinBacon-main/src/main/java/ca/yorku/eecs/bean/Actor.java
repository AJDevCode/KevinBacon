package ca.yorku.eecs.bean;

public class Actor {
    private String name;
    private String actorId;

    public Actor(String name, String actorId) {
        this.name = name;
        this.actorId = actorId;
    }

    public String getName() {
        return name;
    }

    public String getActorId() {
        return actorId;
    }

    @Override
    public String toString() {
        return "Actor{" +
                "name='" + name + '\'' +
                ", actorId='" + actorId + '\'' +
                '}';
    }
}
