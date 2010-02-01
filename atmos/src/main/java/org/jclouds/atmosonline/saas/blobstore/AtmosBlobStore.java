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
package org.jclouds.atmosonline.saas.blobstore;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.atmosonline.saas.AtmosStorageAsyncClient;
import org.jclouds.atmosonline.saas.AtmosStorageClient;
import org.jclouds.atmosonline.saas.blobstore.functions.BlobStoreListOptionsToListOptions;
import org.jclouds.atmosonline.saas.blobstore.functions.BlobToObject;
import org.jclouds.atmosonline.saas.blobstore.functions.DirectoryEntryListToResourceMetadataList;
import org.jclouds.atmosonline.saas.blobstore.functions.ObjectToBlob;
import org.jclouds.atmosonline.saas.blobstore.functions.ObjectToBlobMetadata;
import org.jclouds.atmosonline.saas.options.ListOptions;
import org.jclouds.atmosonline.saas.util.AtmosStorageUtils;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.functions.BlobToHttpGetOptions;
import org.jclouds.blobstore.internal.BaseBlobStore;
import org.jclouds.blobstore.util.BlobStoreUtils;
import org.jclouds.encryption.EncryptionService;
import org.jclouds.http.options.GetOptions;

/**
 * @author Adrian Cole
 */
@Singleton
public class AtmosBlobStore extends BaseBlobStore {
   private final AtmosStorageClient sync;
   private final ObjectToBlob object2Blob;
   private final ObjectToBlobMetadata object2BlobMd;
   private final BlobToObject blob2Object;
   private final BlobStoreListOptionsToListOptions container2ContainerListOptions;
   private final DirectoryEntryListToResourceMetadataList container2ResourceList;
   private final EncryptionService encryptionService;
   private final BlobToHttpGetOptions blob2ObjectGetOptions;

   @Inject
   AtmosBlobStore(BlobStoreUtils blobUtils, AtmosStorageClient sync, ObjectToBlob object2Blob,
            ObjectToBlobMetadata object2BlobMd, BlobToObject blob2Object,
            BlobStoreListOptionsToListOptions container2ContainerListOptions,
            DirectoryEntryListToResourceMetadataList container2ResourceList,
            EncryptionService encryptionService, BlobToHttpGetOptions blob2ObjectGetOptions) {
      super(blobUtils);
      this.blob2ObjectGetOptions = checkNotNull(blob2ObjectGetOptions, "blob2ObjectGetOptions");
      this.sync = checkNotNull(sync, "sync");
      this.container2ContainerListOptions = checkNotNull(container2ContainerListOptions,
               "container2ContainerListOptions");
      this.container2ResourceList = checkNotNull(container2ResourceList, "container2ResourceList");
      this.object2Blob = checkNotNull(object2Blob, "object2Blob");
      this.blob2Object = checkNotNull(blob2Object, "blob2Object");
      this.object2BlobMd = checkNotNull(object2BlobMd, "object2BlobMd");
      this.encryptionService = checkNotNull(encryptionService, "encryptionService");
   }

   /**
    * This implementation invokes {@link AtmosStorageClient#headFile}
    */
   @Override
   public BlobMetadata blobMetadata(String container, String key) {
      return object2BlobMd.apply(sync.headFile(container + "/" + key));
   }

   /**
    * This implementation invokes {@link AtmosStorageAsyncClient#deletePath} followed by
    * {@link AtmosStorageAsyncClient#pathExists} until it is true.
    */
   protected boolean deleteAndVerifyContainerGone(final String container) {
      sync.deletePath(container);
      return !sync.pathExists(container);
   }

   /**
    * This implementation invokes {@link AtmosStorageClient#createDirectory}
    * 
    * @param location
    *           currently ignored
    * @param container
    *           directory name
    */
   @Override
   public boolean createContainerInLocation(String location, String container) {
      sync.createDirectory(container);
      return true;
   }

   /**
    * This implementation invokes {@link AtmosStorageClient#createDirectory}
    * 
    * @param container
    *           directory name
    */
   @Override
   public void createDirectory(String container, String directory) {
      sync.createDirectory(container + "/" + directory);
   }

   /**
    * This implementation invokes {@link AtmosStorageClient#pathExists}
    */
   @Override
   public boolean containerExists(String container) {
      return sync.pathExists(container);
   }

   /**
    * This implementation invokes {@link AtmosStorageClient#pathExists}
    */
   @Override
   public boolean directoryExists(String container, String directory) {
      return sync.pathExists(container + "/" + directory + "/");
   }

   /**
    * This implementation invokes {@link AtmosStorageClient#pathExists}
    * 
    * @param container
    *           container
    * @param key
    *           file name
    */
   @Override
   public boolean blobExists(String container, String key) {
      return sync.pathExists(container + "/" + key);
   }

   /**
    * This implementation invokes {@link AtmosStorageClient#readFile}
    */
   @Override
   public Blob getBlob(String container, String key,
            org.jclouds.blobstore.options.GetOptions options) {
      GetOptions httpOptions = blob2ObjectGetOptions.apply(options);
      return object2Blob.apply(sync.readFile(container + "/" + key, httpOptions));
   }

   /**
    * This implementation invokes {@link AtmosStorageClient#listDirectories}
    */
   @Override
   public PageSet<? extends StorageMetadata> list() {
      return container2ResourceList.apply(sync.listDirectories());
   }

   /**
    * This implementation invokes {@link AtmosStorageClient#listDirectory}
    */
   @Override
   public PageSet<? extends StorageMetadata> list(String container,
            org.jclouds.blobstore.options.ListContainerOptions options) {
      container = AtmosStorageUtils.adjustContainerIfDirOptionPresent(container, options);
      ListOptions nativeOptions = container2ContainerListOptions.apply(options);
      return container2ResourceList.apply(sync.listDirectory(container, nativeOptions));
   }

   /**
    * This implementation invokes {@link AtmosStorageClient#createFile}
    * <p/>
    * Since there is no etag support in atmos, we just return the path.
    */
   @Override
   public String putBlob(final String container, final Blob blob) {
      final String path = container + "/" + blob.getMetadata().getName();
      deleteAndEnsurePathGone(path);
      if (blob.getMetadata().getContentMD5() != null)
         blob.getMetadata().getUserMetadata().put("content-md5",
                  encryptionService.toHexString(blob.getMetadata().getContentMD5()));
      sync.createFile(container, blob2Object.apply(blob));
      return path;
   }

   /**
    * This implementation invokes {@link AtmosStorageClient#deletePath}
    */
   @Override
   public void removeBlob(String container, String key) {
      sync.deletePath(container + "/" + key);
   }

}
