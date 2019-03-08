/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.model;

import java.util.ArrayList;
import java.util.List;

public class RegistrationState {
    private String name;
    private List<RegistrationCenter> centers = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RegistrationCenter> getCenters() {
        return centers;
    }

    public void setCenters(List<RegistrationCenter> centers) {
        this.centers = centers;
    }


    @Override
    public String toString() {
        return "RegistrationState{" +
                "name='" + name + '\'' +
                ", centers=" + centers +
                '}';
    }
}
