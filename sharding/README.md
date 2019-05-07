Square Test Sharding with Kochiku!
==================================

Kochiku has the ability to shard your tests across multiple build workers. This can significantly
improve build times for projects which have long running test suites on the order of 10 minutes or 
more. 

Using this does NOT improve compile times. Every sharded build node must completely and 
independently compile all sources to binaries prior to executing the sharded tests.


How does it work?
-----------------

This module contains a generic JUnit TestSuite that will scan the classpath of your module into 
equally-sized buckets of test classes. The sharding plan is based on the alphabetical ordering of
the class test names. For instance, assuming you have 3 shards configured, the sharding plan would
look something like this:

<pre><code>
  AppleTest             }
  CaramelTest           }-- SHARD 1
  FigTest               }
  
  GrapeTest             }          
  IceCreamTest          }-- SHARD 2
  JuiceTest             }
  
  KrispyKremeTest       }
  PieTest               }-- SHARD 3
  ZippyTest             }
</code></pre>
  
Tests are NOT currently weighted by how many test methods there are per test class. 


How to Use
----------

1. Import this test sharding module in your project by adding the dependency in your `pom.xml`.

<pre><code>
    &lt;dependency&gt;
      &lt;groupId&gt;com.squareup.testing&lt;/groupId&gt;
      &lt;artifactId&gt;sharding&lt;/artifactId&gt;
      &lt;version&gt;HEAD-SNAPSHOT&lt;/version&gt;
    &lt;/dependency&gt;
</code></pre>


2. Add a small stub class to your `tests` classes similar to this one:

<pre><code>
public class FranklinTestSuite {
  public static TestSuite suite() throws Exception {
    return ShardingTestSuite.suite("com.squareup.franklin");
  }
}
</code></pre>

Note that the parameter passed to `ShardingTestSuite.suite()` should reflect your module's top-level
package. Do not simply use `com.squareup` as you may end up running tests from other modules since
this relies on classpath, not relative file path scanning.

3. Configure your `pom.xml` to have Surefire invoke the TestSuite you created.

<pre><code>
   &lt;plugins&gt;
     &lt;plugin&gt;
       &lt;artifactId&gt;maven-surefire-plugin&lt;/artifactId&gt;
       &lt;configuration&gt;
         &lt;includes&gt;
           &lt;include&gt;<strong>**/FranklinTestSuite.java</strong>&lt;/include&gt;
         &lt;/includes&gt;
       &lt;/configuration&gt;
     &lt;/plugin&gt;
   &lt;/plugins&gt;
</code></pre>

NOTE: Assuming you don't have other test patterns explicitly included, Surefire will now only
      run your tests via the test suite.

4. Configure [`kochiku.yml`](../../kochiku.yml) to shard your tests.
 
Inside the [`kochiku.yml`](../../kochiku.yml) you will find a property named `multiple_workers`. Add a sub-property
that corresponds to your module name, along with the number of build workers to shard your build 
across.


<pre><code>
  ...
  multiple_workers:
    my_project_name: 5
  ..  
</code></pre>

5. Open an PR with these changes to test!

Sharding Sub-Modules
--------------------

Some Kochiku builds run test suites for multiple submodules.
To enable sharding for these submodules simply repeat steps 1 through 3 for each submodule.
Only the top-level module needs to be specified in `kochiku.yaml`;
the submodules will inherit that shard count.

