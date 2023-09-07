package ca.yorku.eecs.bean;

public class Movie {
    private String name;
    private String movieId;

    private String genre;

    public Movie(String name, String movieId) {
        this.name = name;
        this.movieId = movieId;
    }

    public String getName() {
        return name;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getMovieId() {
        return movieId;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "name='" + name + '\'' +
                ", movieId='" + movieId + '\'' +
                ", genre='" + genre + '\'' +
                '}';
    }
}
