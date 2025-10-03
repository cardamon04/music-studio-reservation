DO $$ BEGIN
  CREATE TYPE event_type AS ENUM (
    'BookingCreated',
    'BookingConfirmed',
    'BookingCanceled',
    'MemberVerified',
    'CheckedIn',
    'CheckedOut',
    'EquipmentAllocated',
    'EquipmentAllocationReleased',
    'EquipmentCheckedOut',
    'EquipmentReturned'
  );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
