package mill.eval

import mill.define.Target
import mill.discover.Discovered
import mill.discover.Discovered.mapping
import mill.util.{DummyLogger, OSet}
import utest._
import utest.framework.TestPath

object FailureTests extends TestSuite{

  def workspace(implicit tp: TestPath) = {
    ammonite.ops.pwd / 'target / 'workspace / 'failure / implicitly[TestPath].value
  }
  class Checker(mapping: Discovered.Mapping[_])(implicit tp: TestPath){

    val evaluator = new Evaluator(workspace, ammonite.ops.pwd, mapping, DummyLogger)

    def apply(target: Target[_], expectedFailCount: Int, expectedRawValues: Seq[Result[_]]) = {

      val res = evaluator.evaluate(OSet(target))
      assert(
        res.rawValues == expectedRawValues,
        res.failing.keyCount == expectedFailCount
      )

    }
  }
  val tests = Tests{
    val graphs = new mill.util.TestGraphs()
    import graphs._

    'evaluateSingle - {
      ammonite.ops.rm(ammonite.ops.Path(workspace, ammonite.ops.pwd))
      val check = new Checker(mapping(singleton))
      check(
        target = singleton.single,
        expectedFailCount = 0,
        expectedRawValues = Seq(Result.Success(0))
      )

      singleton.single.failure = Some("lols")

      check(
        target = singleton.single,
        expectedFailCount = 1,
        expectedRawValues = Seq(Result.Failure("lols"))
      )

      singleton.single.failure = None

      check(
        target = singleton.single,
        expectedFailCount = 0,
        expectedRawValues = Seq(Result.Success(0))
      )


      val ex = new IndexOutOfBoundsException()
      singleton.single.exception = Some(ex)


      check(
        target = singleton.single,
        expectedFailCount = 1,
        expectedRawValues = Seq(Result.Exception(ex))
      )
    }
    'evaluatePair - {
      ammonite.ops.rm(ammonite.ops.Path(workspace, ammonite.ops.pwd))
      val check = new Checker(mapping(pair))
      check(
        pair.down,
        expectedFailCount = 0,
        expectedRawValues = Seq(Result.Success(0))
      )

      pair.up.failure = Some("lols")

      check(
        pair.down,
        expectedFailCount = 1,
        expectedRawValues = Seq(Result.Skipped)
      )

      pair.up.failure = None

      check(
        pair.down,
        expectedFailCount = 0,
        expectedRawValues = Seq(Result.Success(0))
      )

      pair.up.exception = Some(new IndexOutOfBoundsException())

      check(
        pair.down,
        expectedFailCount = 1,
        expectedRawValues = Seq(Result.Skipped)
      )
    }
  }
}

