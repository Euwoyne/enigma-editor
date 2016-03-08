
/*
  Enigma Editor
  Copyright (C) 2015 Dominik Lehmann
  
  Licensed under the EUPL, Version 1.1 or – as soon they
  will be approved by the European Commission - subsequent
  versions of the EUPL (the "Licence");
  You may not use this work except in compliance with the
  Licence.
  You may obtain a copy of the Licence at:
  
  https://joinup.ec.europa.eu/software/page/eupl
  
  Unless required by applicable law or agreed to in
  writing, software distributed under the Licence is
  distributed on an "AS IS" basis,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  express or implied.
  See the Licence for the specific language governing
  permissions and limitations under the Licence.
*/

package com.github.euwoyne.enigma_edit.lua;

import java.util.Map;

import com.github.euwoyne.enigma_edit.lua.data.CodeSnippet;
import com.github.euwoyne.enigma_edit.lua.data.ConstValue;
import com.github.euwoyne.enigma_edit.lua.data.MMSimpleValue;
import com.github.euwoyne.enigma_edit.lua.data.Mode;
import com.github.euwoyne.enigma_edit.lua.data.Table;
import com.github.euwoyne.enigma_edit.lua.data.Variable;

/**
 * A base interface for all constants, the Enigma API defines.
 * This is just a convenience type, grouping all enums, that represent such
 * constants into one super-type and providing some functions dealing with
 * these constants (code data initialization and integer-to-constant conversion).
 */
public interface Constants
{
	/**
	 * The constant's value.
	 * @return  The integer value of the constant.
	 */
	int value();
	
	/**
	 * Attribute {@code state} of all switch-able objects ({@code ON/OFF}).
	 * Used by
	 *  {@code it_magnet}, 
	 *  {@code it_wormhole}, 
	 *  {@code st_floppy}, 
	 *  {@code st_key}, 
	 *  {@code st_laser}, 
	 *  {@code st_laserflop}, 
	 *  {@code st_laserswitch}, 
	 *  {@code st_lightpassenger}, 
	 *  {@code st_monoflop}, 
	 *  {@code st_polarswitch}, 
	 *  {@code st_switch} and
	 *  {@code st_timer}.
	 */
	public static enum SwitchState implements Constants
	{
		OFF(0), ON(1);
		
		private final int val;
		private SwitchState(int val) {this.val=val;}
		public int value() {return this.val;}
		
		public static SwitchState checkInt(int value)
		{
			if (value == OFF.val) return OFF;
			if (value == ON.val)  return ON;
			                      return null;
		}
	}
	
	/**
	 * Attribute {@code state} of all door-like objects ({@code OPEN/CLOSED}).
	 * Used by
	 *  {@code fl_bridge},
	 *  {@code it_blocker},
	 *  {@code it_trap},
	 *  {@code it_vortex},
	 *  {@code st_blocker},
	 *  {@code st_door} and
	 *  {@code st_fake}.
	 */
	public static enum DoorState implements Constants
	{
		CLOSED(0), OPEN(1);
		
		private final int val;
		private DoorState(int val) {this.val=val;}
		public int value() {return this.val;}
		
		public static DoorState checkInt(int value)
		{
			switch (value)
			{
			case 0:  return CLOSED;
			case 1:  return OPEN;
			default: return null;
			}
		}
	}
	
	/**
	 * Generic attribute {@code state} (with values {@code IDLE/ACTIVE/INACTIVE}).
	 * Used by
	 *  {@code it_dynamite},
	 *  {@code it_bomb},
	 *  {@code st_spitter} and
	 *  {@code st_volcano}.
	 */
	public static enum State implements Constants
	{
		IDLE(0), ACTIVE(1), INACTIVE(2);
		
		private final int val;
		private State(int val) {this.val=val;}
		public int value() {return this.val;}
		
		public static State checkInt2(int value)
		{
			switch (value)
			{
			case 0:  return IDLE;
			case 1:  return ACTIVE;
			default: return null;
			}
		}
		
		public static State checkInt3(int value)
		{
			switch (value)
			{
			case 0:  return IDLE;
			case 1:  return ACTIVE;
			case 2:  return INACTIVE;
			default: return null;
			}
		}
	}
	
	/**
	 * Attribute {@code state} of the oxyd stone {@code st_oxyd} (values {@code CLOSED/OPEN/OXYDPAIR}).
	 */
	public static enum OxydState implements Constants
	{
		CLOSED(0), OPEN(1), OXYDPAIR(2);
		
		private final int val;
		private OxydState(int val) {this.val=val;}
		public int value() {return this.val;}
		
		public static OxydState checkInt(int value)
		{
			switch (value)
			{
			case 0:  return CLOSED;
			case 1:  return OPEN;
			case 2:  return OXYDPAIR;
			default: return null;
			}
		}
	}
	
	/**
	 * Attribute {@code state} of the disco stone {@code st_disco} (values {@code LIGHT/MEDIUM/DARK}).
	 */
	public static enum DiscoState implements Constants
	{
		LIGHT(0), MEDIUM(1), DARK(2);
		
		private final int val;
		private DiscoState(int val) {this.val=val;}
		public int value() {return this.val;}
		
		public static DiscoState checkInt(int value)
		{
			switch (value)
			{
			case 0:  return LIGHT;
			case 1:  return MEDIUM;
			case 2:  return DARK;
			default: return null;
			}
		}
	}
	
	/**
	 * Attribute {@code state} of water {@code fl_water} (values {@code IDLE/FLOODING}).
	 */
	public static enum FloodState implements Constants
	{
		IDLE(0), FLOODING(1);
		
		private final int val;
		private FloodState(int val) {this.val=val;}
		public int value() {return this.val;}
		
		public static FloodState checkInt(int value)
		{
			switch (value)
			{
			case 0:  return IDLE;
			case 1:  return FLOODING;
			default: return null;
			}
		}
	}
	
	/**
	 * Attribute {@code state} of coins {@code it_coin} (values {@code SMALL/MEDIUM/LARGE}).
	 */
	public static enum CoinState implements Constants
	{
		SMALL(0), MEDIUM(1), LARGE(2);
		
		private final int val;
		private CoinState(int val) {this.val=val;}
		public int value() {return this.val;}
		
		public static CoinState checkInt(int value)
		{
			switch (value)
			{
			case 0:  return SMALL;
			case 1:  return MEDIUM;
			case 2:  return LARGE;
			default: return null;
			}
		}
	}
	
	/**
	 * Attribute {@code state} of floor cracks {@code it_crack} (values {@code INVISIBLE/SMALL/MEDIUM/LARGE}).
	 */
	public static enum CrackState implements Constants
	{
		INVISIBLE(-1), SMALL(0), MEDIUM(1), LARGE(2);
		
		private final int val;
		private CrackState(int val) {this.val=val;}
		public int value() {return this.val;}
		
		public static CrackState checkInt(int value)
		{
			switch (value)
			{
			case -1: return INVISIBLE;
			case  0: return SMALL;
			case  1: return MEDIUM;
			case  2: return LARGE;
			default: return null;
			}
		}
	}
	
	/**
	 * Attribute {@code state} of the yin-yang floor {@code fl_yinyang} (values {@code YIN/YANG}).
	 */
	public static enum YinYangState implements Constants
	{
		YIN(0), YANG(1);
		
		private final int val;
		private YinYangState(int val) {this.val=val;}
		public int value() {return this.val;}
		
		public static YinYangState checkInt(int value)
		{
			switch (value)
			{
			case  0: return YIN;
			case  1: return YANG;
			default: return null;
			}
		}
	}
	
	/**
	 * Attribute {@code state} of an extinguisher {@code it_extinguisher} (values {@code EMPTY/MEDIUM/FULL}).
	 */
	public static enum ExtinguisherState implements Constants
	{
		EMPTY(0), MEDIUM(1), FULL(2);
		
		private final int val;
		private ExtinguisherState(int val) {this.val=val;}
		public int value() {return this.val;}
		
		public static ExtinguisherState checkInt(int value)
		{
			switch (value)
			{
			case  0: return EMPTY;
			case  1: return MEDIUM;
			case  2: return FULL;
			default: return null;
			}
		}
	}
	
	/**
	 * Attribute {@code state} of the bottle item {@code it_bottle} (values {@code IDLE/BROKEN}).
	 */
	public static enum BottleState implements Constants
	{
		IDLE(0), BROKEN(1);
		
		private final int val;
		private BottleState(int val) {this.val=val;}
		public int value() {return this.val;}
		
		public static BottleState checkInt(int value)
		{
			switch (value)
			{
			case  0: return IDLE;
			case  1: return BROKEN;
			default: return null;
			}
		}
	}
	
	/**
	 * Attribute {@code state} of the quake stone {@code st_quake} (values {@code IDLE/ACTIVE/BREAKING}).
	 */
	public static enum QuakeState implements Constants
	{
		IDLE(0), ACTIVE(1), BREAKING(2);
		
		private final int val;
		private QuakeState(int val) {this.val=val;}
		public int value() {return this.val;}
		
		public static QuakeState checkInt(int value)
		{
			switch (value)
			{
			case  0: return IDLE;
			case  1: return ACTIVE;
			case  2: return BREAKING;
			default: return null;
			}
		}
	}
	
	/**
	 * Attribute {@code state} indicating direction of the rotator stone {@code st_rotator} (values {@code CW/CCW}).
	 */
	public static enum RotatorState implements Constants
	{
		CW(0), CCW(1);
		
		private final int val;
		private RotatorState(int val) {this.val=val;}
		public int value() {return val;}
		
		public static RotatorState checkInt(int value)
		{
			switch (value)
			{
			case  0: return CW;
			case  1: return CCW;
			default: return null;
			}
		}
	}
	
	/**
	 * Attribute {@code state} of all colored objects ({@code BLACK/WHITE/BLUE/YELLOW/NOCOLOR}).
	 * Used by
	 *  {@code it_flag} (uses black and white),
	 *  {@code it_bomb} (uses black and white),
	 *  {@code st_chess} (uses black and white),
	 *  {@code st_jamb} (uses black and white),
	 *  {@code st_oneway} (uses back, white and nocolor),
	 *  {@code st_passage} (uses black and white),
	 *  {@code st_switch} (uses black, white and nocolor) and
	 *  {@code st_puzzle} (uses blue and yellow).
	 */
	public static enum ColorState implements Constants
	{
		BLACK(0), WHITE(1), BLUE(2), YELLOW(3), NOCOLOR(-1);
		
		private final int val;
		private ColorState(int val) {this.val=val;}
		public int value() {return val;}
		
		public static ColorState checkInt_bw(int value)
		{
			switch (value)
			{
			case  0: return BLACK;
			case  1: return WHITE;
			default: return null;
			}
		}
		
		public static ColorState checkInt_puzzle(int value)
		{
			switch (value)
			{
			case  2: return BLUE;
			case  3: return YELLOW;
			default: return null;
			}
		}
	}
	
	public static enum Controllers implements Constants
	{
		CTRL_NONE(0), CTRL_YIN(1), CTRL_YANG(2), CTRL_YINYANG(3);
		
		private final int val;
		private Controllers(int val) {this.val=val;}
		public int value() {return val;}
		
		public static Controllers checkInt(int value)
		{
			switch (value)
			{
			case  0: return CTRL_NONE;
			case  1: return CTRL_YIN;
			case  2: return CTRL_YANG;
			case  3: return CTRL_YINYANG;
			default: return null;
			}
		}
	}
	
	public static enum OxydColor implements Constants
	{
		OXYD_BLUE(0),  OXYD_RED(1),    OXYD_GREEN(2),  OXYD_YELLOW(3),
		OXYD_CYAN(4),  OXYD_PURPLE(5), OXYD_WHITE(6),  OXYD_BLACK(7),
		OXYD_GRAY(8),  OXYD_ORANGE(9), OXYD_PINE(10),  OXYD_BROWN(11),
		OXYD_AUTO(-1), OXYD_FAKE(-2),  OXYD_QUAKE(-3), OXYD_BOLD(-4);
		
		private final int val;
		private OxydColor(int val) {this.val=val;}
		public int value() {return val;}
		
		public static OxydColor checkInt(int value)
		{
			switch (value)
			{
			case -4: return OXYD_BOLD;
			case -3: return OXYD_QUAKE;
			case -2: return OXYD_FAKE;
			case -1: return OXYD_AUTO;
			case  0: return OXYD_BLUE;
			case  1: return OXYD_RED;
			case  2: return OXYD_GREEN;
			case  3: return OXYD_YELLOW;
			case  4: return OXYD_CYAN;
			case  5: return OXYD_PURPLE;
			case  6: return OXYD_WHITE;
			case  7: return OXYD_BLACK;
			case  8: return OXYD_GRAY;
			case  9: return OXYD_ORANGE;
			case 10: return OXYD_PINE;
			case 11: return OXYD_BROWN;
			default: return null;
			}
		}
	}
	
	public static enum Orientation implements Constants
	{
		RANDOMDIR(-2), NODIR(-1),
		WEST(0),       SOUTH(1),      EAST(2),      NORTH(3),
		NORTHWEST(4),  SOUTHWEST(5),  SOUTHEAST(6), NORTHEAST(7),
		BACKSLASH(8),  HORIZONTAL(9), SLASH(10),    VERTICAL(11);
		
		private final int val;
		private Orientation(int val)       {this.val=val;}
		public int value()                 {return val < 8 ? val : val-8;}
		
		public boolean isDirection4()      {return  val >=  0 && val <  4;}
		public boolean isDirection8()      {return  val >=  0 && val <  8;}
		public boolean isDirection8nodir() {return  val >= -1 && val <  8;}
		public boolean isMirror()          {return (val >=  8 && val < 12) || val == -2;}
		
		public static Orientation checkInt4(int value)
		{
			switch (value)
			{
			case  0: return WEST;
			case  1: return SOUTH;
			case  2: return EAST;
			case  3: return NORTH;
			default: return null;
			}
		}
		
		public static Orientation checkInt8_nodir(int value)
		{
			switch (value)
			{
			case -1: return NODIR;
			case  0: return WEST;
			case  1: return SOUTH;
			case  2: return EAST;
			case  3: return NORTH;
			case  4: return NORTHWEST;
			case  5: return SOUTHWEST;
			case  6: return SOUTHEAST;
			case  7: return NORTHEAST;
			default: return null;
			}
		}
		
		public static Orientation checkInt_mirror(int value)
		{
			switch (value)
			{
			case -2: return RANDOMDIR;
			case  0: return BACKSLASH;
			case  1: return HORIZONTAL;
			case  2: return SLASH;
			case  3: return VERTICAL;
			default: return null;
			}
		}
	}
	
	public static enum Essential implements Constants
	{
		DISPENSABLE(0), INDISPENSABLE(1), PERKIND(2);
		
		private final int val;
		private Essential(int val) {this.val=val;}
		public int value() {return val;}
		
		public static Essential checkInt(int value)
		{
			switch (value)
			{
			case  0: return DISPENSABLE;
			case  1: return INDISPENSABLE;
			default: return null;
			}
		}
		
		public static Essential checkInt_ac(int value)
		{
			switch (value)
			{
			case  0: return DISPENSABLE;
			case  1: return INDISPENSABLE;
			case  2: return PERKIND;
			default: return null;
			}
		}
	}
	
	public static enum MeditationState implements Constants
	{
		MEDITATION_CALDERA(-3), MEDITATION_HOLLOW(-2), MEDITATION_DENT(-1), MEDITATION_BUMP(1), MEDITATION_HILL(2), MEDITATION_VOLCANO(3);
		
		private final int val;
		private MeditationState(int val) {this.val=val;}
		public int value() {return val;}
		
		public static MeditationState checkInt(int value)
		{
			switch (value)
			{
			case -3: return MEDITATION_CALDERA;
			case -2: return MEDITATION_HOLLOW;
			case -1: return MEDITATION_DENT;
			case  1: return MEDITATION_BUMP;
			case  2: return MEDITATION_HILL;
			case  3: return MEDITATION_VOLCANO;
			default: return null;
			}
		}
	}
	
	public static class GlassesState implements Constants
	{
		public static final GlassesState SPOT_NOTHING        = new GlassesState(0);
		public static final GlassesState SPOT_DEATH          = new GlassesState(1);
		public static final GlassesState SPOT_HOLLOW         = new GlassesState(2);
		public static final GlassesState SPOT_ACTORIMPULSE   = new GlassesState(4);
		public static final GlassesState SPOT_SENSOR         = new GlassesState(8);
		public static final GlassesState SPOT_LIGHTPASSENGER = new GlassesState(16);
		public static final GlassesState SPOT_TRAP           = new GlassesState(32);
		
		private final int val;
		private GlassesState(int val) {this.val=val;}
		
		public int          value()                 {return val;}
		public boolean      has(GlassesState state) {return (val & state.val) == state.val;}
		public GlassesState or (GlassesState state) {return new GlassesState(val | state.val);}
		
		public static GlassesState[] values() {return new GlassesState[] {SPOT_NOTHING, SPOT_DEATH, SPOT_HOLLOW, SPOT_ACTORIMPULSE, SPOT_SENSOR, SPOT_LIGHTPASSENGER, SPOT_TRAP};}
		
		public static GlassesState checkInt(int value)
		{
			return (value >= 0 && value < 64) ? new GlassesState(value) : null;
		}
		
		public String toString()
		{
			if (val == 0) return "SPOT_NOTHING";
			StringBuilder out = new StringBuilder();
			if ((val &  1) != 0) out.append("SPOT_DEATH | ");
			if ((val &  2) != 0) out.append("SPOT_HOLLOW | ");
			if ((val &  4) != 0) out.append("SPOT_ACTORIMPULSE | ");
			if ((val &  8) != 0) out.append("SPOT_SENSOR | ");
			if ((val & 16) != 0) out.append("SPOT_LIGHTPASSENGER | ");
			if ((val & 32) != 0) out.append("SPOT_TRAP | ");
			if (out.length() == 0) return "SPOT_NOTHING";
			out.setLength(out.length() - 3);
			return out.toString();
		}
	}
	
	public static enum SubSoil implements Constants
	{
		SUBSOIL_ABYSS(0), SUBSOIL_WATER(1), SUBSOIL_AUTO(2);
		
		private final int val;
		private SubSoil(int val) {this.val=val;}
		public int value() {return val;}
		
		public static SubSoil checkInt(int value)
		{
			switch (value)
			{
			case 0:  return SUBSOIL_ABYSS;
			case 1:  return SUBSOIL_WATER;
			case 2:  return SUBSOIL_AUTO;
			default: return null;
			}
		}
	}
	
	/**
	 * Add instances of all constants to the given data structure.
	 * Each constant defined within this interface and its nested classes,
	 * will be added to the given {@link CodeData} instance as objects of
	 * {@link ConstValue}.
	 * 
	 * @param data  The target data structure.
	 */
	public static void initialize(CodeData data)
	{
		for (SwitchState i       : SwitchState.values())       data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (DoorState i         : DoorState.values())         data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (State i             : State.values())             data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (OxydState i         : OxydState.values())         data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (DiscoState i        : DiscoState.values())        data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (FloodState i        : FloodState.values())        data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (CoinState i         : CoinState.values())         data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (CrackState i        : CrackState.values())        data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (YinYangState i      : YinYangState.values())      data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (ExtinguisherState i : ExtinguisherState.values()) data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (BottleState i       : BottleState.values())       data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (QuakeState i        : QuakeState.values())        data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (RotatorState i      : RotatorState.values())      data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (ColorState i        : ColorState.values())        data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (Controllers i       : Controllers.values())       data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (OxydColor i         : OxydColor.values())         data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (Orientation i       : Orientation.values())       data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (Essential i         : Essential.values())         data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (MeditationState i   : MeditationState.values())   data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (GlassesState i      : GlassesState.values())      data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		for (SubSoil i           : SubSoil.values())           data.assign(i.toString(), new ConstValue(i, CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
	}
	
	/**
	 * Check, if the given kind has attributes, whose values are expected to be
	 * predefined constants.
	 * This is called by {@link #checkConst} to abort checking without
	 * gathering all the data, if the checked tile does not have any attributes
	 * worth checking.
	 * 
	 * @param kind  Kind to check.
	 * @return      {@code true}, if this kind has attributes, which should make use of predefined constants.
	 */
	static boolean isCheckedKind(String kind)
	{
		int i = kind.indexOf('_'); i = kind.indexOf('_', i + 1);
		if (i > 0) kind = kind.substring(0, i);
		if (kind.startsWith("#")) kind = kind.substring(1);
		switch (kind)
		{
		case "ac_bug":        case "ac_horse":   case "ac_killer":   case "ac_marble":     case "ac_pearl":     case "ac_rotor":       case "ac_top":
			
		case "it_blocker":    case "it_bomb":    case "it_bottle":   case "it_coin":       case "it_crack":     case "it_dynamite":    case "it_extinguisher":
		case "it_flag":       case "it_glasses": case "it_magnet":   case "it_meditation": case "it_puller":    case "it_trap":        case "it_vortex":
		case "it_wormhole":
			
		case "st_blocker":    case "st_boulder": case "st_chess":    case "st_disco":      case "st_door":      case "st_fake":        case "st_floppy":
		case "st_fourswitch": case "st_jamb":    case "st_key":      case "st_laser":      case "st_laserflop": case "st_laserswitch": case "st_lightpassenger":
		case "st_mail":       case "st_mirror":  case "st_monoflop": case "st_oneway":     case "st_oxyd":      case "st_passage":     case "st_polarswitch":
		case "st_puzzle":     case "st_quake":   case "st_rotator":  case "st_spitter":    case "st_switch":    case "st_timer":       case "st_turnstilearm":
		case "st_volcano":    case "st_yinyang":
		
		case "fl_bridge":     case "fl_slope":   case "fl_water":    case "fl_yinyang":
			
			return true;
		
		default:
			return false;
		}
	}
	
	/**
	 * Check, if the given attribute prefers predefined constants as its values.
	 * This is called by {@link #checkConst} to abort checking without
	 * gathering all the data, if the checked tile does not have any attributes
	 * worth checking.
	 * 
	 * @param attribute  Attribute to check.
	 * @return           {@code true}, if this attribute might have special constant values.
	 */
	static boolean isCheckedAttribute(String attribute)
	{
		switch (attribute)
		{
		case "state":
		case "color":
		case "controllers":
		case "owner":
		case "oxydcolor":
		case "orientation":
		case "slope":
		case "essential": return true;
		default: return false;
		}
	}
	
	/**
	 * Check, if the given value can be represented by a predefined constant.
	 * This check needs the object kind and the attribute name, to determine,
	 * which set of constants to use.
	 * 
	 * @param kind       Enigma object kind hosting the attribute.
	 * @param attribute  Attribute name.
	 * @param value      Numeric value of the attribute.
	 * @return           A value of a {@link Constants} enum-type,
	 *                   or {@code null}, if the attribute does not use constants. 
	 */
	static Constants checkConst(String kind, String attribute, int value)
	{
		int i = kind.indexOf('_'); i = kind.indexOf('_', i + 1);
		if (i > 0) kind = kind.substring(0, i);
		if (kind.startsWith("#")) kind = kind.substring(1);
		kind = kind + '|' + attribute;
		
		switch (kind)
		{
		case "it_magnet|state":
		case "it_wormhole|state":
		case "st_floppy|state":
		case "st_key|state":
		case "st_laser|state":
		case "st_laserflop|state":
		case "st_laserswitch|state":
		case "st_lightpassenger|state":
		case "st_monoflop|state":
		case "st_polarswitch|state":
		case "st_switch|state":
		case "st_timer|state":              return SwitchState.checkInt(value);
		
		case "fl_bridge|state":
		case "it_blocker|state":
		case "it_trap|state":
		case "it_vortex|state":
		case "st_blocker|state":
		case "st_door|state":
		case "st_fake|state":               return DoorState.checkInt(value);
		
		case "it_dynamite|state":
		case "it_bomb|state":
		case "st_spitter|state":
		case "st_volcano|state":            return State.checkInt2(value);
		
		case "st_yinyang|state":            return State.checkInt3(value);
		case "st_oxyd|state":               return OxydState.checkInt(value);
		case "st_disco|state":              return DiscoState.checkInt(value);
		case "fl_water|state":              return FloodState.checkInt(value);
		case "it_coin|state":               return CoinState.checkInt(value);
		case "it_crack|state":              return CrackState.checkInt(value);
		case "fl_yinyang|state":            return YinYangState.checkInt(value);
		case "it_extinguisher|state":       return ExtinguisherState.checkInt(value);
		case "it_bottle|state":             return BottleState.checkInt(value);
		case "st_quake|state":              return QuakeState.checkInt(value);
		case "st_rotator|state":            return RotatorState.checkInt(value);
		
		case "it_flag|color":
		case "it_bomb|color":
		case "st_chess|color":
		case "st_jamb|color":
		case "st_oneway|color":
		case "st_passage|color":
		case "st_switch|color":             return ColorState.checkInt_bw(value);
		
		case "st_puzzle|color":             return ColorState.checkInt_puzzle(value);
		
		case "ac_bug|controllers":
		case "ac_horse|controllers":
		case "ac_killer|controllers":
		case "ac_marble|controllers":
		case "ac_pearl|controllers":
		case "ac_rotor|controllers":
		case "ac_top|controllers":          return Controllers.checkInt(value);
		
		case "ac_bug|owner":
		case "ac_horse|owner":
		case "ac_killer|owner":
		case "ac_marble|owner":
		case "ac_pearl|owner":
		case "ac_rotor|owner":
		case "ac_top|owner":                return YinYangState.checkInt(value);
		
		case "st_oxyd|oxydcolor":           return OxydColor.checkInt(value);
		
		case "it_puller|orientation":
		case "st_boulder|orientation":
		case "st_fourswitch|state":
		case "st_laser|orientation":
		case "st_mail|orientation":
		case "st_oneway|orientation":
		case "st_turnstilearm|orientation": return Orientation.checkInt4(value);
		
		case "fl_slope|slope":              return Orientation.checkInt8_nodir(value);
		case "st_mirror|orientation":       return Orientation.checkInt_mirror(value);
		case "it_meditation|essential":     return Essential.checkInt(value);
		
		case "ac_bug|essential":
		case "ac_horse|essential":
		case "ac_killer|essential":
		case "ac_marble|essential":
		case "ac_pearl|essential":
		case "ac_rotor|essential":
		case "ac_top|essential":            return Essential.checkInt_ac(value);
		
		case "it_meditation|state":         return MeditationState.checkInt(value);
		case "it_glasses|state":            return GlassesState.checkInt(value);
		
		default:                            return null;
		}
	}
	
	/**
	 * Check a tile's attributes for integer literals, that can be converted to constants.
	 * If the given table represents a tile, its attributes are checked by {@link #checkConst}
	 * and possibly converted to the given constant.
	 * 
	 * @param val  A table, that defines a {@link TileDecl tile}
	 *             (or a {@link TileDeclPart part thereof}).
	 */
	public static void checkTable(Table val)
	{
		if (val.exist(1))
		{
			final MMSimpleValue kind = val.get(1).checkSimple();
			String easyKind = kind.hasEasy()      ? kind.easy.value.checkjstring()      : null;
			String diffKind = kind.hasDifficult() ? kind.difficult.value.checkjstring() : null;
			if (!Constants.isCheckedKind(easyKind)) easyKind = null;
			if (!Constants.isCheckedKind(diffKind)) diffKind = null;
			if (easyKind != null || diffKind != null)
			{
				for (Map.Entry<String, Variable> entry : val)
				{
					if (entry.getKey().charAt(0) != '"') continue;
					final String key = entry.getKey().substring(1, entry.getKey().length() - 1);
					if (!Constants.isCheckedAttribute(key)) continue;
					final MMSimpleValue mmattrval = entry.getValue().checkSimple();
					if (mmattrval.hasNormal())
					{
						if (mmattrval.easy instanceof ConstValue) continue;
						if (mmattrval.easy.value.isinttype())
						{
	 						if (easyKind.equals(diffKind))
	 						{
	 							final Constants c = Constants.checkConst(easyKind, key, mmattrval.easy.value.checkint());
		 						if (c != null)
		 							entry.getValue().assign(new ConstValue(c, entry.getValue().easy.getCode()), entry.getValue().getAssign(Mode.NORMAL), Mode.NORMAL);
	 						}
	 						else
	 						{
	 							final Constants ceasy = Constants.checkConst(easyKind, key, mmattrval.easy.value.checkint());
	 							final Constants cdiff = Constants.checkConst(diffKind, key, mmattrval.easy.value.checkint());
		 						if (ceasy != null)
		 							entry.getValue().assign(new ConstValue(ceasy, entry.getValue().easy.getCode()), entry.getValue().getAssign(Mode.EASY), Mode.EASY);
		 						if (cdiff != null)
		 							entry.getValue().assign(new ConstValue(ceasy, entry.getValue().easy.getCode()), entry.getValue().getAssign(Mode.DIFFICULT), Mode.DIFFICULT);
	 						}
						}
					}
					else
					{
						if (mmattrval.hasEasy() && !(mmattrval.easy instanceof ConstValue) && mmattrval.easy.value.isinttype())
						{
	 						final Constants c = Constants.checkConst(easyKind, key, mmattrval.easy.value.checkint());
	 						if (c != null)
	 							entry.getValue().assign(new ConstValue(c, entry.getValue().easy.getCode()), entry.getValue().getAssign(Mode.EASY), Mode.EASY);
						}
						if (mmattrval.hasDifficult() && !(mmattrval.difficult instanceof ConstValue) && mmattrval.difficult.value.isinttype())
						{
	 						final Constants c = Constants.checkConst(diffKind, key, mmattrval.difficult.value.checkint());
	 						if (c != null)
	 							entry.getValue().assign(new ConstValue(c, entry.getValue().difficult.getCode()), entry.getValue().getAssign(Mode.DIFFICULT), Mode.DIFFICULT);
						}
					}
				}
			}
		}
	}
}

