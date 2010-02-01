/**
 *
 * Copyright (C) 2009 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package org.jclouds.azure.storage.blob.domain.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import org.jclouds.azure.storage.blob.domain.AzureBlob;
import org.jclouds.azure.storage.blob.domain.MutableBlobProperties;
import org.jclouds.encryption.EncryptionService;
import org.jclouds.http.internal.BasePayloadEnclosingImpl;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * Default Implementation of {@link AzureBlob}.
 * 
 * @author Adrian Cole
 */
public class AzureBlobImpl extends BasePayloadEnclosingImpl implements AzureBlob,
         Comparable<AzureBlob> {
   private final MutableBlobProperties properties;
   private Multimap<String, String> allHeaders = LinkedHashMultimap.create();

   @Inject
   public AzureBlobImpl(EncryptionService encryptionService, MutableBlobProperties properties) {
      super(encryptionService);
      this.properties = properties;
   }

   @Override
   protected void setContentMD5(byte[] md5) {
      getProperties().setContentMD5(md5);
   }

   /**
    * {@inheritDoc}
    */
   public MutableBlobProperties getProperties() {
      return properties;
   }

   /**
    * {@inheritDoc}
    */
   public Multimap<String, String> getAllHeaders() {
      return allHeaders;
   }

   /**
    * {@inheritDoc}
    */
   public void setAllHeaders(Multimap<String, String> allHeaders) {
      this.allHeaders = checkNotNull(allHeaders, "allHeaders");
   }

   /**
    * {@inheritDoc}
    */
   public int compareTo(AzureBlob o) {
      if (getProperties().getName() == null)
         return -1;
      return (this == o) ? 0 : getProperties().getName().compareTo(o.getProperties().getName());
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((allHeaders == null) ? 0 : allHeaders.hashCode());
      result = prime * result + ((contentLength == null) ? 0 : contentLength.hashCode());
      result = prime * result + ((payload == null) ? 0 : payload.hashCode());
      result = prime * result + ((properties == null) ? 0 : properties.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      AzureBlobImpl other = (AzureBlobImpl) obj;
      if (allHeaders == null) {
         if (other.allHeaders != null)
            return false;
      } else if (!allHeaders.equals(other.allHeaders))
         return false;
      if (contentLength == null) {
         if (other.contentLength != null)
            return false;
      } else if (!contentLength.equals(other.contentLength))
         return false;
      if (payload == null) {
         if (other.payload != null)
            return false;
      } else if (!payload.equals(other.payload))
         return false;
      if (properties == null) {
         if (other.properties != null)
            return false;
      } else if (!properties.equals(other.properties))
         return false;
      return true;
   }

   @Override
   public String toString() {
      return "[properties=" + properties + "]";
   }

}
