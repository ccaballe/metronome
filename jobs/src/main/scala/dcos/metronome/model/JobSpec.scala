package dcos.metronome.model

import com.wix.accord.dsl._
import com.wix.accord.Validator
import dcos.metronome.utils.glue.MarathonConversions
import mesosphere.marathon
import mesosphere.marathon.api.v2.Validation._
import mesosphere.marathon.plugin.{ Secret, EnvVarValue, RunSpec }

import scala.collection.immutable._

case class JobSpec(
    id:          JobId,
    description: Option[String]      = JobSpec.DefaultDescription,
    labels:      Map[String, String] = JobSpec.DefaultLabels,
    schedules:   Seq[ScheduleSpec]   = JobSpec.DefaultSchedule,
    run:         JobRunSpec          = JobSpec.DefaultRunSpec
) extends RunSpec {
  def schedule(id: String): Option[ScheduleSpec] = schedules.find(_.id == id)

  override def user: Option[String] = run.user
  override def acceptedResourceRoles: Option[Predef.Set[String]] = None
  override def secrets: Map[String, Secret] = MarathonConversions.secretsToMarathon(run.secrets)
  override def env: Map[String, marathon.state.EnvVarValue] = MarathonConversions.envVarToMarathon(run.env)
}

object JobSpec {
  val DefaultDescription = None
  val DefaultLabels = Map.empty[String, String]
  val DefaultSchedule = Seq.empty[ScheduleSpec]
  val DefaultRunSpec = JobRunSpec()

  implicit lazy val validJobSpec: Validator[JobSpec] = validator[JobSpec] { jobSpec =>
    jobSpec.id is valid
    jobSpec.schedules is every(valid)
    jobSpec.schedules has size <= 1 // FIXME: we will support only one schedule in v1
  }
}

