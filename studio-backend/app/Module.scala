import com.google.inject.{AbstractModule, Provides, Singleton}
import scala.concurrent.ExecutionContext
import application.booking.*
import infrastructure.booking.*
import infrastructure.equipment.*
import infrastructure.reservationtype.*
import infrastructure.student.*
import infrastructure.period.*
import domain.period.*
import domain.equipment.*
import domain.reservationtype.*
import domain.student.*

class Module extends AbstractModule {
  override def configure(): Unit = {
    // リポジトリのバインディング
    bind(classOf[BookingRepository])
      .to(classOf[InMemoryBookingRepository])
      .in(classOf[Singleton])

    bind(classOf[EquipmentRepository])
      .to(classOf[InMemoryEquipmentRepository])
      .in(classOf[Singleton])

    bind(classOf[ReservationTypeRepository])
      .to(classOf[InMemoryReservationTypeRepository])
      .in(classOf[Singleton])

    bind(classOf[StudentRepository])
      .to(classOf[InMemoryStudentRepository])
      .in(classOf[Singleton])

    bind(classOf[PeriodTimeRepository])
      .to(classOf[InMemoryPeriodTimeRepository])
      .in(classOf[Singleton])
  }

  @Provides @Singleton
  def provideQueryService(
      bookingRepository: InMemoryBookingRepository,
      reservationTypeRepository: ReservationTypeRepository,
      ec: ExecutionContext
  ): BookingCalendarQueryService = {
    new InMemoryBookingCalendarQueryService(
      bookingRepository,
      reservationTypeRepository
    )(ec)
  }
}
