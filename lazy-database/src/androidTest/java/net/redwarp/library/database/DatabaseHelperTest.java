/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright 2015 Redwarp
 */

package net.redwarp.library.database;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import junit.framework.Assert;

import net.redwarp.library.database.test.MyClass;
import net.redwarp.library.database.test.Test;
import net.redwarp.library.database.test.TestLink;

import java.util.List;
import java.util.Random;

public class DatabaseHelperTest extends AndroidTestCase {

  Context context;
  DatabaseHelper helper;

  @Override
  protected void setUp() throws Exception {
    context = new RenamingDelegatingContext(getContext(), "test_");
    context.deleteDatabase(context.getString(R.string._lazy_database_base_name));

    helper = new DatabaseHelper(context);
  }

  @Override
  protected void tearDown() throws Exception {
    helper.close();
  }

  public void testCreateTest() {
    Test test = new Test();

    test.length = 19;
    test.randomReal = Math.PI;

    boolean didSave = helper.save(test);
    Assert.assertTrue("Did save", didSave);
  }

  public void testEntryCount() {
    Test test1 = new Test();
    Test test2 = new Test();
    helper.clear(Test.class);
    helper.beginTransaction();

    helper.save(test1);
    helper.save(test2);
    List<Test> allTests = helper.getAll(Test.class);

    helper.endTransaction();

    Assert.assertEquals("Number of entries", 2, allTests.size());
  }

  public void testSerialization() {
    Test test = new Test();
    test.length = 19;
    test.randomReal = Math.PI;

    helper.save(test);

    Assert.assertTrue("Id is set", test.id > 0);

    test = helper.getWithId(Test.class, test.id);

    Assert.assertEquals("Length should be 19", 19, test.length);
  }

  public void testClear() {
    helper.save(new Test());
    helper.clear(Test.class);
    List<Test> allTests = helper.getAll(Test.class);
    Assert.assertEquals("Number of entries", 0, allTests.size());
  }

  public void testTransaction() {
    helper.clear(Test.class);
    Random random = new Random();
    int capacity = 5000;
    helper.beginTransaction();
    for (int i = 0; i < capacity; i++) {
      Test test = new Test();
      test.randomReal = random.nextDouble();
      test.length = random.nextInt();
      helper.save(test);
    }
    helper.setTransactionSuccessful();
    helper.endTransaction();

    List<Test> entries = helper.getAll(Test.class);
    Assert.assertEquals("Number of entries", capacity, entries.size());
    helper.clear(Test.class);
  }


  public void testCount() {
    helper.clear(MyClass.class);

    final long count = 10;
    BaseAdapter.getOpenHelper().getWritableDatabase().beginTransaction();
    for (int index = 0; index < count; index++) {
      MyClass test = new MyClass();
      helper.save(test);
    }
    BaseAdapter.getOpenHelper().getWritableDatabase().setTransactionSuccessful();
    BaseAdapter.getOpenHelper().getWritableDatabase().endTransaction();

    Assert.assertEquals("Item count", count, helper.getCount(MyClass.class));
  }


  public void testChainClear() {
    helper.clear(TestLink.class);
    helper.clear(Test.class);

    helper.save(new Test());

    Assert.assertEquals("Item count", 1, helper.getCount(TestLink.class));

    helper.clear(Test.class);

    Assert.assertEquals("Item count", 0, helper.getCount(TestLink.class));
  }
}
