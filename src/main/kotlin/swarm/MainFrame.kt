package swarm

import java.awt.*
import java.awt.Color
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.util.concurrent.ThreadLocalRandom
import javax.swing.JFrame
import javax.swing.WindowConstants

class MainFrame : JFrame("Swarm") {
    private val image: BufferedImage
    private val canvas: Canvas
    private val tracer: Tracer
    private var spheres = mutableListOf<Sphere>()
    private val eye = Vector3(0.0, 0.0, 200.0)

    @Override
    override fun paint(g: Graphics) = (g as Graphics2D).drawImage(image, null, 0, 0)

    private fun simulate(seconds: Double) {
        if (spheres.size < 92) {
            val offset = 128.0
            val rnd = ThreadLocalRandom.current()
            val x = rnd.nextDouble(-offset, offset)
            val y = rnd.nextDouble(-offset, offset)
            val z = rnd.nextDouble(-offset, offset)
            spheres.add(Sphere(Vector3(x, y, z), 10.0, swarm.Color.randomColor(0.05)))
        }

        for (sphere in spheres) {
            sphere.applyForce(0.0, 0.0, 0.0)
            sphere.integrate(seconds)
        }
        val backColor = Color(background)
        canvas.blend(backColor)
        tracer.trace(eye, spheres, backColor)
        canvas.blur()
        repaint()
    }

    init {
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        background = Color(0xD6, 0xEF, 0xBD)
        size = Dimension(960, 540)
        isResizable = false
        layout = null

        addWindowListener(object : WindowAdapter() {
            @Override
            override fun windowOpened(e: WindowEvent) {
                super.windowOpened(e)
                Toolkit.getDefaultToolkit().systemEventQueue.push(object : EventQueue() {
                    var warmup = 0
                    var tock = System.nanoTime()

                    @Override
                    override fun dispatchEvent(event: AWTEvent) {
                        super.dispatchEvent(event)
                        if (peekEvent() == null) {
                            val tick = System.nanoTime()
                            if (++warmup > 4) simulate((tick - tock) / 1000000000.0)
                            tock = tick
                        }
                    }
                })
            }
        })

        image = BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB_PRE)
        canvas = Canvas(size.width, size.height, (image.raster.dataBuffer as DataBufferInt).data)
        tracer = Tracer(canvas)

        tracer.lights.add(Light(Vector3(+100.0, 300.0, 300.0), Color(0.7, 0.7, 0.7)))
        tracer.lights.add(Light(Vector3(-300.0, -500.0, 30.0), Color(0.295, 0.144, 0.123)))

        isVisible = true
        setLocationRelativeTo(null)
    }
}
