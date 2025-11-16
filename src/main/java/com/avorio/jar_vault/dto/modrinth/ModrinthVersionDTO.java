package com.avorio.jar_vault.dto.modrinth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ModrinthVersionDTO {
    @JsonProperty("id")
    private String id;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("version_number")
    private String versionNumber;

    @JsonProperty("changelog")
    private String changelog;

    @JsonProperty("dependencies")
    private List<DependencyDTO> dependencies;

    @JsonProperty("game_versions")
    private List<String> gameVersions;

    @JsonProperty("version_type")
    private String versionType;

    @JsonProperty("loaders")
    private List<String> loaders;

    @JsonProperty("featured")
    private Boolean featured;

    @JsonProperty("downloads")
    private Long downloads;

    @JsonProperty("date_published")
    private String datePublished;

    @JsonProperty("files")
    private List<FileDTO> files;

    public ModrinthVersionDTO() {
    }

    public static class DependencyDTO {
        @JsonProperty("version_id")
        private String versionId;

        @JsonProperty("project_id")
        private String projectId;

        @JsonProperty("dependency_type")
        private String dependencyType; // required, optional, incompatible, embedded

        @JsonProperty("file_name")
        private String fileName;

        public String getVersionId() {
            return versionId;
        }

        public void setVersionId(String versionId) {
            this.versionId = versionId;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getDependencyType() {
            return dependencyType;
        }

        public void setDependencyType(String dependencyType) {
            this.dependencyType = dependencyType;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }

    public static class FileDTO {
        @JsonProperty("hashes")
        private HashesDTO hashes;

        @JsonProperty("url")
        private String url;

        @JsonProperty("filename")
        private String filename;

        @JsonProperty("primary")
        private Boolean primary;

        @JsonProperty("size")
        private Long size;

        public HashesDTO getHashes() {
            return hashes;
        }

        public void setHashes(HashesDTO hashes) {
            this.hashes = hashes;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public Boolean getPrimary() {
            return primary;
        }

        public void setPrimary(Boolean primary) {
            this.primary = primary;
        }

        public Long getSize() {
            return size;
        }

        public void setSize(Long size) {
            this.size = size;
        }
    }

    public static class HashesDTO {
        @JsonProperty("sha512")
        private String sha512;

        @JsonProperty("sha1")
        private String sha1;

        public String getSha512() {
            return sha512;
        }

        public void setSha512(String sha512) {
            this.sha512 = sha512;
        }

        public String getSha1() {
            return sha1;
        }

        public void setSha1(String sha1) {
            this.sha1 = sha1;
        }
    }
}

