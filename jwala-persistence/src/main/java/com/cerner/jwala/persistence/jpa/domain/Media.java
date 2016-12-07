package com.cerner.jwala.persistence.jpa.domain;

import com.cerner.jwala.persistence.jpa.type.MediaType;

import javax.persistence.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * POJO that defines a media such as jdk, tomcat application server, apache web server
 *
 * Created by Jedd Anthony Cuison on 12/6/2016
 */
@Entity
public class Media extends AbstractEntity<Media> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private MediaType type;

    private String localPath;

    private String remotePath;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public MediaType getType() {
        return type;
    }

    public void setType(final MediaType type) {
        this.type = type;
    }

    public Path getLocalPath() {
        return Paths.get(localPath);
    }

    public void setLocalPath(final Path localPath) {
        this.localPath = localPath.toString();
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(final String remotePath) {
        this.remotePath = remotePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Media media = (Media) o;

        return id.equals(media.id) && name.equals(media.name);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Media{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", localPath='" + localPath + '\'' +
                ", remotePath='" + remotePath + '\'' +
                '}';
    }

}
