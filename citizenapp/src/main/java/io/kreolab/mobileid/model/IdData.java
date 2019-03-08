/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class IdData implements Parcelable {

    private String idNumber ="N\\A";
    private String surname="N\\A";
    private String firstname="N\\A";
    private String other_names ="N\\A";
    private long date_of_birth;
    private String nationality="N\\A";
    private long date_of_issue;
    private long date_of_expiry;
    private byte[] photo;
    private String pin;

    public IdData(){

    }

    protected IdData(Parcel in) {
        idNumber = in.readString();
        surname = in.readString();
        firstname = in.readString();
        other_names = in.readString();
        date_of_birth = in.readLong();
        nationality = in.readString();
        date_of_issue = in.readLong();
        date_of_expiry = in.readLong();
        photo = in.createByteArray();
        pin = in.readString();
    }


    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getOther_names() {
        return other_names;
    }

    public void setOther_names(String other_names) {
        this.other_names = other_names;
    }

    public long getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(long date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public long getDate_of_issue() {
        return date_of_issue;
    }

    public void setDate_of_issue(long date_of_issue) {
        this.date_of_issue = date_of_issue;
    }

    public long getDate_of_expiry() {
        return date_of_expiry;
    }

    public void setDate_of_expiry(long date_of_expiry) {
        this.date_of_expiry = date_of_expiry;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idNumber);
        dest.writeString(surname);
        dest.writeString(firstname);
        dest.writeString(other_names);
        dest.writeLong(date_of_birth);
        dest.writeString(nationality);
        dest.writeLong(date_of_issue);
        dest.writeLong(date_of_expiry);
        dest.writeByteArray(photo);
        dest.writeString(pin);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<IdData> CREATOR = new Creator<IdData>() {
        @Override
        public IdData createFromParcel(Parcel in) {
            return new IdData(in);
        }

        @Override
        public IdData[] newArray(int size) {
            return new IdData[size];
        }
    };


    @Override
    public String toString() {
        return "IdData{" +
                "idNumber='" + idNumber + '\'' +
                ", surname='" + surname + '\'' +
                ", firstname='" + firstname + '\'' +
                ", other_names='" + other_names + '\'' +
                '}';
    }
}
