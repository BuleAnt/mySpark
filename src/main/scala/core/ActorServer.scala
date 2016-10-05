package core

object ActorServer extends App {

	import akka.actor.{Actor, Props, ActorSystem}

	implicit val system = ActorSystem()

	var i = 10
	var add = (n: Int) => n + 1
	var decrease = (n: Int) => n - 1

	class EchoServer extends Actor {
		def receive = {
			case "+" => {
				i = add(i)
			}
			case "-" => i = decrease(i)
			case "show" =>
				println(i)
		}
	}

	var ops = (1 to 4).map(x => system.actorOf(Props(new EchoServer())))
	for ((op, index) <- ops.view.zipWithIndex) {
		if (index % 2 == 0) op ! "+" else op ! "-"
	}
	ops(0) ! "show"
	system.shutdown

}