/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jxmapviewer;

import java.util.Objects;

/**
 * The map mode
 */
public class MVEMode {

    private String type;
    private String ext;
    private String name;
    private String label;

    public MVEMode(final String name, final String label, final String type, final String ext) {
        this.type = type;
        this.ext = ext;
        this.name = name;
        this.label = label;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public String getExt() {
        return ext;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.type);
        hash = 79 * hash + Objects.hashCode(this.ext);
        hash = 79 * hash + Objects.hashCode(this.name);
        hash = 79 * hash + Objects.hashCode(this.label);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MVEMode other = (MVEMode) obj;
        if (!Objects.equals(this.type, other.getType())) {
            return false;
        }
        if (!Objects.equals(this.ext, other.getExt())) {
            return false;
        }
        if (!Objects.equals(this.name, other.getName())) {
            return false;
        }
        return Objects.equals(this.label, other.getLabel());
    }

    @Override
    public String toString() {
        return "MVEMode{" + "type=" + type + ", ext=" + ext + ", name=" + name + ", label=" + label + '}';
    }
  
}
