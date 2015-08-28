package com.ravenrobotics.android.movies.app;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {

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

    private Movie(Parcel in) {
        title = in.readString();
        posterPath = in.readString();
        releaseDate = in.readString();
        voteAverage = in.readString();
        overview = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(posterPath);
        out.writeString(releaseDate);
        out.writeString(voteAverage);
        out.writeString(overview);
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

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        } // end method createFromParcel
        public Movie[] newArray(int size) {
            return new Movie[size];
        } // end method newArray
    };

} // end class Movie
