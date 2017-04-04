package io.mifos.rhythm.api.v1.events;

import java.util.Objects;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BeatEvent {
  private String applicationName;
  private String beatIdentifier;

  public BeatEvent() {
  }

  public BeatEvent(String applicationName, String beatIdentifier) {
    this.applicationName = applicationName;
    this.beatIdentifier = beatIdentifier;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public String getBeatIdentifier() {
    return beatIdentifier;
  }

  public void setBeatIdentifier(String beatIdentifier) {
    this.beatIdentifier = beatIdentifier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BeatEvent beatEvent = (BeatEvent) o;
    return Objects.equals(applicationName, beatEvent.applicationName) &&
            Objects.equals(beatIdentifier, beatEvent.beatIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationName, beatIdentifier);
  }

  @Override
  public String toString() {
    return "BeatEvent{" +
            "applicationName='" + applicationName + '\'' +
            ", beatIdentifier='" + beatIdentifier + '\'' +
            '}';
  }
}
