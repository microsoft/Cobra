
import threading
import queue


class SingleThreadExecutor(threading.Thread):
	def __init__(self):
		threading.Thread.__init__(self)
		self.daemon = True
		self.queue = queue.Queue()
		self.start()

	def run(self):
		while True:
			(task, args) = self.queue.get()
			task(args)
			self.queue.task_done()

	def submit(self, task, args):
		self.queue.put((task, args))

	def join(self):
		self.queue.join()

executor = SingleThreadExecutor()

class Deferred(object):
	IN_PROGRESS = 0
	RESOLVED = 1
	REJECTED = 2

	def __init__(self):
		self.lock = threading.Lock()
		self.state = Deferred.IN_PROGRESS
		self.data = None
		self.resolves = []
		self.rejects = []
		self.updates = []
		
	def add(self, resolve=None, reject=None, update=None):
		self.lock.acquire()
		try:
			if self.state == Deferred.RESOLVED:
				if resolve:
					self._submit(resolve, self.data)
				return
			if self.state == Deferred.REJECTED:
				if reject:
					self._submit(reject, self.data)
				return
			if resolve:
				self.resolves.append(resolve)
			if reject:
				self.rejects.append(reject)
			if update:
				self.updates.append(update)
		finally:
			self.lock.release()

	def resolve(self, data=None):
		self.lock.acquire()
		try:
			if self.state:
				return
			self.state = Deferred.RESOLVED
			self.data = data
			for resolve in self.resolves:
				self._submit(resolve, data)
		finally:
			self.lock.release()

	def reject(self, data=None):
		self.lock.acquire()
		try:
			if self.state:
				return
			self.state = Deferred.REJECTED
			self.data = data
			for reject in self.rejects:
				self._submit(reject, data)
		finally:
			self.lock.release()

	def update(self, data=None):
		self.lock.acquire()
		try:
			if self.state:
				return
			for update in self.updates:
				self._submit(update, data)
		finally:
			self.lock.release()


	def _submit(self, task, args):
		global executor
		executor.submit(task, args)



