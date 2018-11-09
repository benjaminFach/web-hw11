package com.bfach2;

import java.time.LocalDate;

/**
 * Represents a tour reservation
 */
public class Entry {

  // class attributes (all required)
  private LocalDate startDate;
  private String location;
  private String guide;
  private String firstName;
  private String lastName;

  // default constructor, private so Builder is used
  private Entry(Builder entryBuilder) {
    this.startDate = entryBuilder.startDate;
    this.location = entryBuilder.location;
    this.guide = entryBuilder.guide;
    this.firstName = entryBuilder.firstName;
    this.lastName = entryBuilder.lastName;
  }

  // Builder pattern for generating entries
  public static class Builder {

    private LocalDate startDate;
    private String location;
    private String guide;
    private String firstName;
    private String lastName;

    public Builder startDate(LocalDate startDate) {
      this.startDate = startDate;
      return this;
    }

    public Builder location(String location) {
      this.location = location;
      return this;
    }

    public Builder guide(String guide) {
      this.guide = guide;
      return this;
    }

    public Builder firstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder lastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public Entry build() {
      return new Entry(this);
    }
  }

  @Override
  public String toString() {
    return guide + " | " + location + " | " + firstName + " | " + lastName + " | " + startDate;
  }
}
