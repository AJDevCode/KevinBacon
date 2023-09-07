package ca.yorku.eecs.bean;

public class Relationship {
    private String actorId;
    private String movieId;

    public Relationship(String actorId, String movieId) {
        this.actorId = actorId;
        this.movieId = movieId;
    }

    public String getActorId() {
        return actorId;
    }

    public String getMovieId() {
        return movieId;
    }

    @Override
    public String toString() {
        return "Relationship{" +
                "actorId='" + actorId + '\'' +
                ", movieId='" + movieId + '\'' +
                '}';
    }
}
