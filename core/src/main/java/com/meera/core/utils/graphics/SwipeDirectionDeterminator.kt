package com.meera.core.utils.graphics

import android.graphics.PointF
import android.util.Range
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * [SwipeDirectionDeterminator] определяет направление свайпа исходя из двух точек.
 */
enum class SwipeDirectionDeterminator {

    UP, DOWN, LEFT, RIGHT;

    companion object {

        /**
         * Вычисляет направление свайпа исходя из двух точек.
         */
        fun getDirection(pointA: PointF, pointB: PointF): SwipeDirectionDeterminator {
            val angle = getAngle(pointA = pointA, pointB = pointB)
            return angle.getDirection()
        }

        /**
         * Делает сброс координат точки.
         */
        fun PointF.reset() = set(0f, 0f)

        /**
         * Вычисляет разность координат двух точек.
         */
        infix fun PointF.sub(point: PointF) = PointF(x - point.x, y - point.y)

        /**
         * Вычисляет угол между двумя точками в градусах.
         */
        fun getAngle(pointA: PointF, pointB: PointF): Float {
            val deltaY = pointA.y - pointB.y
            val deltaX = pointB.x - pointA.x
            val rad = atan2(y = deltaY, x = deltaX) + Math.PI
            val angle = (rad * 180 / Math.PI + 180) % 360
            return angle.toFloat()
        }

        /**
         * Вычисляет радиус-вектор между двумя точками.
         */
        fun getRadiusVector(pointA: PointF, pointB: PointF): Float {
            val dXSquared = (pointA.x - pointB.x).pow(2)
            val dYSquared = (pointA.y - pointB.y).pow(2)
            return sqrt(dXSquared + dYSquared)
        }

        /**
         * Вычисляет пройденное расстояние с помощью точки [distanceTraveled].
         * Проверяет выходит ли оно за пределы touchSlop
         * @param touchSlopSquared квадрат touchSlop
         * @param distanceTraveled точка, содержащая перемещение из одних координат в другие
         * @return true если вышли за пределы touchSlop, иначе false
         */
        fun isMotionBeyondTouchSlop(touchSlopSquared: Int, distanceTraveled: PointF): Boolean {
            return isMotionBeyondTouchSlop(
                touchSlopSquared = touchSlopSquared,
                distanceTraveledX = distanceTraveled.x,
                distanceTraveledY = distanceTraveled.y
            )
        }

        /**
         * Вычисляет пройденное расстояние с помощью перемещения по осям X, Y.
         * Проверяет выходит ли оно за пределы touchSlop
         * @param touchSlopSquared квадрат touchSlop
         * @param distanceTraveledX перемещение по Х оси
         * @param distanceTraveledY перемещение по Y оси
         * @return true если вышли за пределы touchSlop, иначе false
         */
        fun isMotionBeyondTouchSlop(touchSlopSquared: Int, distanceTraveledX: Float, distanceTraveledY: Float): Boolean {
            return abs(distanceTraveledX) > touchSlopSquared || abs(distanceTraveledY) > touchSlopSquared
        }

        private fun Float.getDirection(): SwipeDirectionDeterminator {
            return when {
                inRange(range = getUpRange()) -> UP
                inRange(range = getStartRightRange()) || inRange(range = getEndRightRange()) -> RIGHT
                inRange(range = getDownRange()) -> DOWN
                else -> LEFT
            }
        }

        private fun getStartRightRange() = Range(0f, 45f)

        private fun getEndRightRange() = Range(285f, 360f)

        private fun getUpRange() = Range(45f, 135f)

        private fun getDownRange() = Range(255f, 285f)

        private fun Float.inRange(range: Range<Float>) = this >= range.lower && this < range.upper
    }
}
