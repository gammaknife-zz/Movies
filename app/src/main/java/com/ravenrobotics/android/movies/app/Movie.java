package com.ravenrobotics.android.movies.app;

public class Movie {

    String title;
    String posterPath;
    String releaseDate;
    String voteAverage;
    String overview;

    public Movie(String title,
                 String posterPath,
                 String releaseDate,
                 String voteAverage,
                 String overview) {
        this.title = title;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.overview = overview;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public String getOverview() {
        return overview;
    }

} // end class Movie
