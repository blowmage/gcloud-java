/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gcloud.storage;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import com.google.api.services.storage.model.Bucket;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

/**
 * Cross-Origin Resource Sharing (CORS) configuration for a bucket.
 */
public final class Cors implements Serializable {

  private static final long serialVersionUID = -8637770919343335655L;

  static final Function<Bucket.Cors, Cors> FROM_PB_FUNCTION = new Function<Bucket.Cors, Cors>() {
    @Override
    public Cors apply(Bucket.Cors pb) {
      return Cors.fromPb(pb);
    }
  };

  static final Function<Cors, Bucket.Cors> TO_PB_FUNCTION = new Function<Cors, Bucket.Cors>() {
    @Override
    public Bucket.Cors apply(Cors cors) {
      return cors.toPb();
    }
  };

  private final Integer maxAgeSeconds;
  private final ImmutableList<HttpMethod> methods;
  private final ImmutableList<Origin> origins;
  private final ImmutableList<String> responseHeaders;

  public static final class Origin implements Serializable {

    private static final long serialVersionUID = -4447958124895577993L;
    private static final String ANY_URI = "*";
    private final String value;

    private static final Origin ANY = new Origin(ANY_URI);

    private Origin(String value) {
      this.value = checkNotNull(value);
    }

    public static Origin any() {
      return ANY;
    }

    public static Origin of(String scheme, String host, int port) {
      try {
        return of(new URI(scheme, null, host, port, null, null, null).toString());
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException(e);
      }
    }

    public static Origin of(String value) {
      if (ANY_URI.equals(value)) {
        return any();
      }
      return new Origin(value);
    }

    @Override
    public int hashCode() {
      return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Origin)) {
        return false;
      }
      return value.equals(((Origin)obj).value);
    }

    @Override
    public String toString() {
      return value();
    }

    public String value() {
      return value;
    }
  }

  public static final class Builder {

    private Integer maxAgeSeconds;
    private ImmutableList<HttpMethod> methods;
    private ImmutableList<Origin> origins;
    private ImmutableList<String> responseHeaders;

    private Builder() {}

    public Builder maxAgeSeconds(Integer maxAgeSeconds) {
      this.maxAgeSeconds = maxAgeSeconds;
      return this;
    }

    public Builder methods(Iterable<HttpMethod> methods) {
      this.methods = methods != null ? ImmutableList.copyOf(methods) : null;
      return this;
    }

    public Builder origins(Iterable<Origin> origins) {
      this.origins = origins != null ? ImmutableList.copyOf(origins) : null;
      return this;
    }

    public Builder responseHeaders(Iterable<String> headers) {
      this.responseHeaders = headers != null ? ImmutableList.copyOf(headers) : null;
      return this;
    }

    public Cors build() {
      return new Cors(this);
    }
  }

  private Cors(Builder builder) {
    this.maxAgeSeconds = builder.maxAgeSeconds;
    this.methods = builder.methods;
    this.origins = builder.origins;
    this.responseHeaders = builder.responseHeaders;
  }

  public Integer maxAgeSeconds() {
    return maxAgeSeconds;
  }

  public List<HttpMethod> methods() {
    return methods;
  }

  public List<Origin> origins() {
    return origins;
  }

  public List<String> responseHeaders() {
    return responseHeaders;
  }

  public Builder toBuilder() {
    return builder()
        .maxAgeSeconds(maxAgeSeconds)
        .methods(methods)
        .origins(origins)
        .responseHeaders(responseHeaders);
  }

  @Override
  public int hashCode() {
    return Objects.hash(maxAgeSeconds, methods, origins, responseHeaders);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Cors)) {
      return false;
    }
    Cors other = (Cors) obj;
    return Objects.equals(maxAgeSeconds, other.maxAgeSeconds)
        && Objects.equals(methods, other.methods)
        && Objects.equals(origins, other.origins)
        && Objects.equals(responseHeaders, other.responseHeaders);
  }

  public static Builder builder() {
    return new Builder();
  }

  Bucket.Cors toPb() {
    Bucket.Cors pb = new Bucket.Cors();
    pb.setMaxAgeSeconds(maxAgeSeconds);
    pb.setResponseHeader(responseHeaders);
    if (methods != null) {
      pb.setMethod(newArrayList(transform(methods, Functions.toStringFunction())));
    }
    if (origins != null) {
      pb.setOrigin(newArrayList(transform(origins, Functions.toStringFunction())));
    }
    return pb;
  }

  static Cors fromPb(Bucket.Cors cors) {
    Builder builder = builder().maxAgeSeconds(cors.getMaxAgeSeconds());
    if (cors.getMethod() != null) {
      builder.methods(transform(cors.getMethod(), new Function<String, HttpMethod>() {
        @Override
        public HttpMethod apply(String name) {
          return HttpMethod.valueOf(name.toUpperCase());
        }
      }));
    }
    if (cors.getOrigin() != null) {
      builder.origins(transform(cors.getOrigin(), new Function<String, Origin>() {
        @Override
        public Origin apply(String value) {
          return Origin.of(value);
        }
      }));
    }
    builder.responseHeaders(cors.getResponseHeader());
    return builder.build();
  }
}
