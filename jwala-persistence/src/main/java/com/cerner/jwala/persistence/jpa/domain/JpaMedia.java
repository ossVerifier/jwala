package com.cerner.jwala.persistence.jpa.domain;

import com.cerner.jwala.persistence.jpa.type.MediaType;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * POJO that defines a media such as jdk, tomcat application server, apache web server
 *
 * Created by Jedd Cuison on 12/6/2016
 */
@Entity(name = "media")
@NamedQueries({@NamedQuery(name = JpaMedia.QUERY_FIND_BY_NAME, query = "SELECT m FROM media m WHERE m.name = :name")})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class JpaMedia extends AbstractEntity<JpaMedia> {

    public static final String QUERY_FIND_BY_NAME = "QUERY_FIND_BY_NAME";
    public static final String PARAM_NAME = "name";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private MediaType type;

    private String localPath;

    private String remoteDir; // e.g. c:/ctp

    private String mediaDir;  // e.g. tomcat-7.0

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

    @JsonSerialize(using = PathToStringSerializer.class)
    public Path getLocalPath() {
        return Paths.get(localPath);
    }

    @JsonDeserialize(using = StringToPathDeserializer.class)
    public void setLocalPath(final Path localPath) {
        this.localPath = localPath.toString();
    }

    @JsonSerialize(using = PathToStringSerializer.class)
    public Path getRemoteDir() {
        return Paths.get(remoteDir);
    }

    @JsonDeserialize(using = StringToPathDeserializer.class)
    public void setRemoteDir(final Path remoteDir) {
        this.remoteDir = remoteDir.toString();
    }

    @JsonSerialize(using = PathToStringSerializer.class)
    public Path getMediaDir() {
        return Paths.get(mediaDir);
    }

    @JsonDeserialize(using = StringToPathDeserializer.class)
    public void setMediaDir(final Path mediaDir) {
        this.mediaDir = mediaDir.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JpaMedia media = (JpaMedia) o;

        return id.equals(media.id) && name.equals(media.name);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (localPath != null ? localPath.hashCode() : 0);
        result = 31 * result + (remoteDir != null ? remoteDir.hashCode() : 0);
        result = 31 * result + (mediaDir != null ? mediaDir.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Media{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", localPath='" + localPath + '\'' +
                ", remoteDir='" + remoteDir + '\'' +
                ", mediaDir='" + mediaDir + '\'' +
                '}';
    }

}
