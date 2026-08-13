CREATE TYPE "role" AS ENUM (
    'teacher',
    'student',
    'admin'
);

CREATE TYPE "specialization" AS ENUM (
    'el',
    'tn'
);

CREATE TABLE IF NOT EXISTS "user" (
    "id" UUID NOT NULL,
    "ref" VARCHAR(255) NOT NULL UNIQUE,
    "firstname" VARCHAR(255),
    "lastname" VARCHAR(255),
    "email" VARCHAR(255) NOT NULL UNIQUE,
    "password" VARCHAR(255) NOT NULL,
    "role" role NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "career" (
    "id" UUID NOT NULL,
    "title" VARCHAR(255),
    "specialization" specialization,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "promotion" (
    "id" UUID NOT NULL,
    "label" VARCHAR(255) NOT NULL,
    "start_year" INTEGER NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "semester" (
    "id" UUID NOT NULL,
    "promotion_id" UUID NOT NULL,
    "number" INTEGER NOT NULL,
    "start_date" TIMESTAMPTZ,
    "end_date" TIMESTAMPTZ,
    PRIMARY KEY ("id"),
    FOREIGN KEY ("promotion_id") REFERENCES "promotion"("id")
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "group" (
    "id" UUID NOT NULL,
    "ref" VARCHAR(255) NOT NULL UNIQUE,
    "promotion_id" UUID NOT NULL,
    "career_id" UUID NOT NULL,
    PRIMARY KEY ("id"),
    FOREIGN KEY ("promotion_id") REFERENCES "promotion"("id")
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY ("career_id") REFERENCES "career"("id")
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS "course" (
    "id" UUID NOT NULL,
    "ref" VARCHAR(255) NOT NULL UNIQUE,
    "title" VARCHAR(255),
    "credits" INTEGER,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "career_course" (
    "id" UUID NOT NULL,
    "career_id" UUID NOT NULL,
    "course_id" UUID NOT NULL,
    "semester_number" INTEGER NOT NULL,
    PRIMARY KEY ("id"),
    UNIQUE ("career_id", "course_id"),
    FOREIGN KEY ("career_id") REFERENCES "career"("id")
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY ("course_id") REFERENCES "course"("id")
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS "user_group" (
    "id" UUID NOT NULL,
    "user_id" UUID NOT NULL,
    "group_id" UUID NOT NULL,
    "start_date" TIMESTAMPTZ NOT NULL DEFAULT now(),
    "end_date" TIMESTAMPTZ,
    PRIMARY KEY ("id"),
    FOREIGN KEY ("user_id") REFERENCES "user"("id")
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY ("group_id") REFERENCES "group"("id")
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "user_promotion" (
    "id" UUID NOT NULL,
    "user_id" UUID NOT NULL,
    "promotion_id" UUID NOT NULL,
    "graduated" BOOLEAN NOT NULL DEFAULT FALSE,
    "graduation_date" TIMESTAMPTZ,
    PRIMARY KEY ("id"),
    UNIQUE ("user_id", "promotion_id"),
    FOREIGN KEY ("user_id") REFERENCES "user"("id")
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY ("promotion_id") REFERENCES "promotion"("id")
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "course_teacher" (
    "id" UUID NOT NULL,
    "course_id" UUID NOT NULL,
    "teacher_id" UUID NOT NULL,
    PRIMARY KEY ("id"),
    UNIQUE ("course_id", "teacher_id"),
    FOREIGN KEY ("course_id") REFERENCES "course"("id")
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY ("teacher_id") REFERENCES "user"("id")
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "course_group" (
    "id" UUID NOT NULL,
    "course_id" UUID NOT NULL,
    "teacher_id" UUID NOT NULL,
    "group_id" UUID NOT NULL,
    PRIMARY KEY ("id"),
    UNIQUE ("course_id", "teacher_id", "group_id"),
    FOREIGN KEY ("course_id") REFERENCES "course"("id")
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY ("teacher_id") REFERENCES "user"("id")
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY ("group_id") REFERENCES "group"("id")
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "exam" (
    "id" UUID NOT NULL,
    "course_id" UUID NOT NULL,
    "title" VARCHAR(255),
    "start_date" TIMESTAMPTZ DEFAULT now(),
    "end_date" TIMESTAMPTZ DEFAULT now(),
    "coefficient" NUMERIC(3,2) NOT NULL,
    PRIMARY KEY ("id"),
    FOREIGN KEY ("course_id") REFERENCES "course"("id")
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "grade" (
    "id" UUID NOT NULL,
    "exam_id" UUID NOT NULL,
    "student_id" UUID NOT NULL,
    "value" NUMERIC(4,2) NOT NULL,
    "assignment_date" TIMESTAMPTZ DEFAULT now(),
    "description" TEXT,
    PRIMARY KEY ("id"),
    UNIQUE ("exam_id", "student_id"),
    FOREIGN KEY ("exam_id") REFERENCES "exam"("id")
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY ("student_id") REFERENCES "user"("id")
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "grade_history" (
    "id" UUID NOT NULL,
    "grade_id" UUID NOT NULL,
    "old_value" NUMERIC(4,2),
    "new_value" NUMERIC(4,2) NOT NULL,
    "reason" TEXT NOT NULL,
    "modified_by" UUID NOT NULL,
    "modified_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY ("id"),
    FOREIGN KEY ("grade_id") REFERENCES "grade"("id")
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY ("modified_by") REFERENCES "user"("id")
        ON UPDATE CASCADE ON DELETE RESTRICT
);
