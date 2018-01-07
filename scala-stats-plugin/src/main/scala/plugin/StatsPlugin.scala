package plugin

import java.io.FileOutputStream
import java.nio.channels.FileLock

import scala.collection.mutable
import scala.tools.nsc.Global
import scala.tools.nsc.ast.TreeDSL
import scala.tools.nsc.plugins.{Plugin, PluginComponent}
import scala.tools.nsc.transform.Transform


class StatsPlugin(val global: Global) extends Plugin {
  val name = "scala-stats-plugin"
  val description = "Gathers function arities defined in a project"
  val components = new StatsTransform(this, global) :: Nil
}

class Stats {
  val funcs = new mutable.ArrayBuffer[(String, Int)]()
  lazy val sorted = funcs.sortBy(_._2)
  def getMean = if (funcs.isEmpty) 0.0 else funcs.foldLeft(0.0){case (s, (_, n)) => s + n} / funcs.length
  def getMax = if (funcs.isEmpty) 0 else sorted.last._2
  def getPercentile(p: Int) = if (funcs.isEmpty) 0.0 else {
    val s = Math.round(funcs.length * (p.toDouble / 100.0)).toInt
    sorted.take(s).foldLeft(0.0){case (s, (_, n)) => s + n} / s
  }
  // I know, but it's good enough
  def save() = {
    val fw = new FileOutputStream("stats.txt", true)
    var l: FileLock = null
    try {
      l = fw.getChannel.lock() // need to lock, 'cause sbt may compile projects in parallel
      funcs.foreach { case (name, cnt) =>
        fw.write(s"$name $cnt\n".getBytes("UTF-8"))
      }
    } catch {
      case ex: Exception => ex.printStackTrace()
    } finally {
      if (l != null) l.release()
      fw.close()
    }
  }
}


class StatsTransform(plugin: Plugin, val global: Global) extends PluginComponent with Transform with TreeDSL {
  import global._

  val runsAfter = "parser" :: Nil
  val phaseName = "code-stats"
  val stats = new Stats()

  override def newPhase(prev: scala.tools.nsc.Phase): Phase = new Phase(prev) {
    override def run(): Unit = {
      super.run()
      global.inform(f"!! Mean: ${stats.getMean}%.2f, max: ${stats.getMax}%d, 50%%: ${stats.getPercentile(50)}%.2f, 75%%: ${stats.getPercentile(75)}%.2f, 90%%: ${stats.getPercentile(90)}%.2f")
      stats.save()
    }
  }

  // I know it doesn't transform the tree. Doesn't matter.
  def newTransformer(unit: CompilationUnit) = new Transformer() {
    override def transform(tree: Tree): Tree = {
      tree match {
        case t@DefDef(mods, name, tparams, vparamss, tpt, rhs) =>
          val num = vparamss.flatten.length
          stats.funcs.append((name.toString, num))
          tree
        case other => super.transform(other)
      }
    }
  }
}
