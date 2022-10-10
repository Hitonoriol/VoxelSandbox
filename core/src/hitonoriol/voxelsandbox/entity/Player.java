package hitonoriol.voxelsandbox.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;

import hitonoriol.voxelsandbox.VoxelSandbox;
import hitonoriol.voxelsandbox.assets.Models;
import hitonoriol.voxelsandbox.assets.Prefs;
import hitonoriol.voxelsandbox.input.GameInput;
import hitonoriol.voxelsandbox.input.PlayerController;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;

public class Player extends Creature {
	private PerspectiveCamera camera = new PerspectiveCamera(Prefs.values().cameraFov,
			Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	private PlayerController controller = new PlayerController(this);
	private Vector3 tmpVec = new Vector3();

	public Player() {
		super(Models.player);
		initBody(info -> {
			info.setMass(1);
			info.setLinearSleepingThreshold(0);
			info.setAngularSleepingThreshold(0);
		}, Vector3.Zero);
		setMovementSpeed(10);
		VoxelSandbox.deferInit(this::initCamera);
	}

	@Override
	protected btCollisionShape createDefaultCollisionShape() {
		return new btCapsuleShape(getDepth() * 0.25f, getHeight() * 0.45f);
	}

	public void setViewDistance(float distance) {
		camera.far = distance + 25f;
		VoxelSandbox.world().environment
				.set(new FogAttribute(FogAttribute.FogEquation).set(camera.near, distance, 1f));
	}

	private void initCamera() {
		camera.near = 0.01f;
		setDirection(camera.direction);
		setViewDistance(Prefs.values().cameraViewDistance);
		syncCamera();
	}

	public PlayerController getController() {
		return controller;
	}

	public PerspectiveCamera getCamera() {
		return camera;
	}

	private void syncCamera() {
		updateCamera();
		updateRotation();
	}

	public void updateRotation() {
		tmpVec.set(camera.direction.x, 0, camera.direction.z).nor();
		rotate(Vector3.X, tmpVec);
	}

	@Override
	protected void rotationChanged() {}

	public void updateCamera() {
		camera.position.set(getPOV());
		camera.update();
	}

	private Vector3 getPOV() {
		var prefs = Prefs.values();
		if (prefs.firstPersonCamera) {
			tmpVec.set(getDirection()).nor().scl(prefs.firstPersonHorizontalDistance);
			tmpVec.y = 0;
			tmpVec.add(getPosition());
			tmpVec.y += getHeight() * prefs.firstPersonVerticalFactor;
			return tmpVec;
		} else {
			var dirVector = getDirection();
			var distance = prefs.thirdPersonHorizontalDistance;
			return tmpVec.set(getPosition())
					.add(-dirVector.x * distance,
							getHeight() + prefs.thirdPersonVerticalDistance,
							-dirVector.z * distance);
		}
	}

	@Override
	protected void positionChanged() {
		super.positionChanged();
		updateCamera();
	}
	
	@Override
	public void dispose() {
		GameInput.unregister(controller);
		super.dispose();
	}
}
