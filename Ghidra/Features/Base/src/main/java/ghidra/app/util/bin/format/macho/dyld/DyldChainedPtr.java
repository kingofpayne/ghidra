/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.app.util.bin.format.macho.dyld;

import ghidra.program.model.address.Address;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.mem.MemoryAccessException;

/**
 * @see <a href="https://opensource.apple.com/source/dyld/dyld-852.2/include/mach-o/fixup-chains.h.auto.html">mach-o/fixup-chains.h</a> 
 */
public class DyldChainedPtr {

	public enum DyldChainType {
		DYLD_CHAINED_PTR_ARM64E(1),    // stride 8, unauth target is vmaddr
		DYLD_CHAINED_PTR_64(2),    // target is vmaddr
		DYLD_CHAINED_PTR_32(3),
		DYLD_CHAINED_PTR_32_CACHE(4),
		DYLD_CHAINED_PTR_32_FIRMWARE(5),
		DYLD_CHAINED_PTR_64_OFFSET(6),   // target is vm offset
		DYLD_CHAINED_PTR_ARM64E_KERNEL(7),   // stride 4, unauth target is vm offset
		DYLD_CHAINED_PTR_64_KERNEL_CACHE(8),
		DYLD_CHAINED_PTR_ARM64E_USERLAND(9),   // stride 8, unauth target is vm offset
		DYLD_CHAINED_PTR_ARM64E_FIRMWARE(10),   // stride 4, unauth target is vmaddr
		DYLD_CHAINED_PTR_X86_64_KERNEL_CACHE(11),   // stride 1, x86_64 kernel caches
		DYLD_CHAINED_PTR_ARM64E_USERLAND24(12),   // stride 8, unauth target is vm offset, 24-bit bind
		DYLD_CHAINED_PTR_TYPE_UNKNOWN(-1);

		private final int val;
		private final String name;

		private DyldChainType(int v) {
			val = v;
			name = this.name().substring("DYLD_CHAINED_".length());
		}

		public static DyldChainType lookupChainPtr(int val) {
			switch (val) {
				case 1:
					return DYLD_CHAINED_PTR_ARM64E;
				case 2:
					return DYLD_CHAINED_PTR_64;
				case 3:
					return DYLD_CHAINED_PTR_32;
				case 4:
					return DYLD_CHAINED_PTR_32_CACHE;
				case 5:
					return DYLD_CHAINED_PTR_32_FIRMWARE;
				case 6:
					return DYLD_CHAINED_PTR_64_OFFSET;
				case 7:
					return DYLD_CHAINED_PTR_ARM64E_KERNEL;
				case 8:
					return DYLD_CHAINED_PTR_64_KERNEL_CACHE;
				case 9:
					return DYLD_CHAINED_PTR_ARM64E_USERLAND;
				case 10:
					return DYLD_CHAINED_PTR_ARM64E_FIRMWARE;
				case 11:
					return DYLD_CHAINED_PTR_X86_64_KERNEL_CACHE;
				case 12:
					return DYLD_CHAINED_PTR_ARM64E_USERLAND24;
			}
			return DYLD_CHAINED_PTR_TYPE_UNKNOWN;
		}

		public int getValue() {
			return val;
		}

		public String getName() {
			return name;
		}
	}

	public static final int DYLD_CHAINED_PTR_START_NONE = 0xFFFF;
	public static final int DYLD_CHAINED_PTR_START_MULTI = 0x8000;
	public static final int DYLD_CHAINED_PTR_START_LAST = 0x8000;

	public static long getStride(DyldChainType ptrFormat) {
		switch (ptrFormat) {
			case DYLD_CHAINED_PTR_ARM64E:
			case DYLD_CHAINED_PTR_ARM64E_USERLAND:
			case DYLD_CHAINED_PTR_ARM64E_USERLAND24:
				return 8;
			case DYLD_CHAINED_PTR_64:
			case DYLD_CHAINED_PTR_64_OFFSET:
			case DYLD_CHAINED_PTR_ARM64E_KERNEL:
			case DYLD_CHAINED_PTR_64_KERNEL_CACHE:
			case DYLD_CHAINED_PTR_ARM64E_FIRMWARE:
			case DYLD_CHAINED_PTR_32:
			case DYLD_CHAINED_PTR_32_CACHE:
			case DYLD_CHAINED_PTR_32_FIRMWARE:
				return 4;
			case DYLD_CHAINED_PTR_X86_64_KERNEL_CACHE:
				return 1;
			default:
				return 1;
		}
	}

	public static void setChainValue(Memory memory, Address chainLoc, DyldChainType ptrFormat,
			long value) throws MemoryAccessException {
		switch (ptrFormat) {
			case DYLD_CHAINED_PTR_ARM64E:
			case DYLD_CHAINED_PTR_ARM64E_USERLAND:
			case DYLD_CHAINED_PTR_ARM64E_USERLAND24:
			case DYLD_CHAINED_PTR_64:
			case DYLD_CHAINED_PTR_64_OFFSET:
			case DYLD_CHAINED_PTR_ARM64E_KERNEL:
			case DYLD_CHAINED_PTR_64_KERNEL_CACHE:
			case DYLD_CHAINED_PTR_ARM64E_FIRMWARE:
			case DYLD_CHAINED_PTR_X86_64_KERNEL_CACHE:
				memory.setLong(chainLoc, value);
				break;

			case DYLD_CHAINED_PTR_32:
			case DYLD_CHAINED_PTR_32_CACHE:
			case DYLD_CHAINED_PTR_32_FIRMWARE:
				memory.setInt(chainLoc, (int) (value & 0xFFFFFFFFL));
				break;
			default:
				break;
		}
	}

	public static long getChainValue(Memory memory, Address chainLoc, DyldChainType ptrFormat)
			throws MemoryAccessException {
		switch (ptrFormat) {
			case DYLD_CHAINED_PTR_ARM64E:
			case DYLD_CHAINED_PTR_ARM64E_USERLAND:
			case DYLD_CHAINED_PTR_ARM64E_USERLAND24:
			case DYLD_CHAINED_PTR_64:
			case DYLD_CHAINED_PTR_64_OFFSET:
			case DYLD_CHAINED_PTR_ARM64E_KERNEL:
			case DYLD_CHAINED_PTR_64_KERNEL_CACHE:
			case DYLD_CHAINED_PTR_ARM64E_FIRMWARE:
			case DYLD_CHAINED_PTR_X86_64_KERNEL_CACHE:
				return memory.getLong(chainLoc);

			case DYLD_CHAINED_PTR_32:
			case DYLD_CHAINED_PTR_32_CACHE:
			case DYLD_CHAINED_PTR_32_FIRMWARE:
				return memory.getInt(chainLoc) & 0xFFFFFFFFL;
			default:
				return 0;
		}
	}

	public static boolean isBound(DyldChainType ptrFormat, long chainValue) {

		switch (ptrFormat) {
			case DYLD_CHAINED_PTR_ARM64E:
			case DYLD_CHAINED_PTR_ARM64E_USERLAND:
			case DYLD_CHAINED_PTR_ARM64E_USERLAND24:
				return ((chainValue >>> 62) & 1) != 0;

			case DYLD_CHAINED_PTR_64:
			case DYLD_CHAINED_PTR_64_OFFSET:
				return ((chainValue >>> 63) & 1) != 0;

			case DYLD_CHAINED_PTR_32:
				return ((chainValue >>> 31) & 1) != 0;

			// Never bound
			case DYLD_CHAINED_PTR_ARM64E_FIRMWARE:
			case DYLD_CHAINED_PTR_ARM64E_KERNEL:
			case DYLD_CHAINED_PTR_X86_64_KERNEL_CACHE:
			case DYLD_CHAINED_PTR_64_KERNEL_CACHE:
			case DYLD_CHAINED_PTR_32_CACHE:
			case DYLD_CHAINED_PTR_32_FIRMWARE:
			default:
				return false;
		}
	}

	public static boolean isAuthenticated(DyldChainType ptrFormat, long chainValue) {
		switch (ptrFormat) {
			case DYLD_CHAINED_PTR_64:
			case DYLD_CHAINED_PTR_64_OFFSET:
			case DYLD_CHAINED_PTR_32:
			case DYLD_CHAINED_PTR_32_CACHE:
			case DYLD_CHAINED_PTR_32_FIRMWARE:
				return false;
			default:
				break;
		}

		boolean isAuthenticated = ((chainValue >>> 63) & 1) != 0;

		return isAuthenticated;
	}

	public static long getDiversity(DyldChainType ptrFormat, long chainValue) {
		if (!isAuthenticated(ptrFormat, chainValue)) {
			return 0;
		}

		long diversityData = (chainValue >> 32L) & 0xFFFFL;

		return diversityData;
	}

	public static boolean hasAddrDiversity(DyldChainType ptrFormat, long chainValue) {
		if (!isAuthenticated(ptrFormat, chainValue)) {
			return false;
		}

		return ((chainValue >> 48L) & 0x1L) == 1;
	}

	public static long getKey(DyldChainType ptrFormat, long chainValue) {
		if (!isAuthenticated(ptrFormat, chainValue)) {
			return 0;
		}

		return (chainValue >> 49L) & 0x3L;
	}

	public static long getTarget(DyldChainType ptrFormat, long chainValue) {

		long target = 0;
		if (isBound(ptrFormat, chainValue)) {
			return -1;
		}

		if (isAuthenticated(ptrFormat, chainValue)) {
			switch (ptrFormat) {
				case DYLD_CHAINED_PTR_ARM64E:
				case DYLD_CHAINED_PTR_ARM64E_USERLAND:
				case DYLD_CHAINED_PTR_ARM64E_USERLAND24:
				case DYLD_CHAINED_PTR_ARM64E_KERNEL:
					return chainValue & 0x000000FFFFFFFFL;
				case DYLD_CHAINED_PTR_X86_64_KERNEL_CACHE:
				case DYLD_CHAINED_PTR_64_KERNEL_CACHE:
					return (chainValue & 0x3FFFFFFFL); // 30 bits
				default:
					break;
			}
		}

		switch (ptrFormat) {
			case DYLD_CHAINED_PTR_ARM64E:
			case DYLD_CHAINED_PTR_ARM64E_USERLAND:
			case DYLD_CHAINED_PTR_ARM64E_USERLAND24:
			case DYLD_CHAINED_PTR_ARM64E_KERNEL:
				long top8Bits = (chainValue >> 43) & 0xFFL;
				long bottom43Bits = chainValue & 0x000007FFFFFFFFFFL;
				// Hack! Top bits don't matter and are a pointer tag
				if (top8Bits == 0x80) {
					top8Bits = 0;
				}
				target = (top8Bits << 56) | bottom43Bits;
				break;

			case DYLD_CHAINED_PTR_64:
			case DYLD_CHAINED_PTR_64_OFFSET:
				top8Bits = (chainValue >> 36) & 0xFFL;
				long bottom36Bits = chainValue & 0x00000FFFFFFFFFL;
				// Hack! Top bits don't matter and are a pointer tag
				if (top8Bits == 0x80) {
					top8Bits = 0;
				}
				target = (top8Bits << 56) | bottom36Bits;
				break;

			case DYLD_CHAINED_PTR_32:
				target = (chainValue & 0x3FFFFFL); // 26 bits
				break;

			case DYLD_CHAINED_PTR_32_CACHE:
				target = (chainValue & 0x3FFFFFFFL); // 30 bits
				break;

			case DYLD_CHAINED_PTR_32_FIRMWARE:
				target = (chainValue & 0x3FFFFFL); // 26 bits
				break;

			case DYLD_CHAINED_PTR_X86_64_KERNEL_CACHE:
			case DYLD_CHAINED_PTR_64_KERNEL_CACHE:
				target = (chainValue & 0x3FFFFFFFL); // 30 bits
				break;
			default:
				return 0;
		}

		return target;
	}

	public static long getAddend(DyldChainType ptrFormat, long chainValue) {

		if (!isBound(ptrFormat, chainValue)) {
			return 0;
		}

		switch (ptrFormat) {
			case DYLD_CHAINED_PTR_ARM64E:
			case DYLD_CHAINED_PTR_ARM64E_USERLAND:
			case DYLD_CHAINED_PTR_ARM64E_USERLAND24:
				long addend = (chainValue >> 32L) & 0x7FFFFL;
				addend = ((addend & 0x40000) != 0 ? (addend | 0xFFFFFFFFFFFC0000L) : addend);
				return addend;

			case DYLD_CHAINED_PTR_64:
			case DYLD_CHAINED_PTR_64_OFFSET:
				return (chainValue >> 24) & 0xFFL;

			case DYLD_CHAINED_PTR_32:
				return (chainValue >> 20) & 0x3FL;  // 6 bits
			default:
				return 0;
		}
	}

	public static long getOrdinal(DyldChainType ptrFormat, long chainValue) {

		long ordinal = -1;
		if (!isBound(ptrFormat, chainValue)) {
			return -1;
		}

		switch (ptrFormat) {
			case DYLD_CHAINED_PTR_ARM64E:
			case DYLD_CHAINED_PTR_ARM64E_USERLAND:
				ordinal = (int) (chainValue & 0xFFFFL);
				break;

			case DYLD_CHAINED_PTR_ARM64E_USERLAND24:
			case DYLD_CHAINED_PTR_64:
			case DYLD_CHAINED_PTR_64_OFFSET:
				ordinal = (int) (chainValue & 0xFFFFFFL);
				break;

			case DYLD_CHAINED_PTR_32:
				ordinal = (int) (chainValue & 0xFFFFFL);
				break;

			// Never Ordinal
			case DYLD_CHAINED_PTR_ARM64E_FIRMWARE:
			case DYLD_CHAINED_PTR_ARM64E_KERNEL:
			case DYLD_CHAINED_PTR_X86_64_KERNEL_CACHE:
			case DYLD_CHAINED_PTR_64_KERNEL_CACHE:
			case DYLD_CHAINED_PTR_32_CACHE:
			case DYLD_CHAINED_PTR_32_FIRMWARE:
				break;
			default:
				break;
		}

		return ordinal;
	}

	public static long getNext(DyldChainType ptrFormat, long chainValue) {

		long next = 1;

		switch (ptrFormat) {
			case DYLD_CHAINED_PTR_ARM64E:
			case DYLD_CHAINED_PTR_ARM64E_USERLAND:
			case DYLD_CHAINED_PTR_ARM64E_USERLAND24:
			case DYLD_CHAINED_PTR_ARM64E_KERNEL:
				next = (chainValue & (0x7FFL << 51L)) >> 51L;   // 11-bits
				break;
			case DYLD_CHAINED_PTR_64:
			case DYLD_CHAINED_PTR_64_OFFSET:
			case DYLD_CHAINED_PTR_X86_64_KERNEL_CACHE:
			case DYLD_CHAINED_PTR_64_KERNEL_CACHE:
				next = (chainValue & (0xFFFL << 52L)) >> 52L;  // 12 bits
				break;

			case DYLD_CHAINED_PTR_32:
				next = (chainValue & (0x1FL << 26L)) >> 26L;  // 5 bits
				break;

			// Never bound
			case DYLD_CHAINED_PTR_ARM64E_FIRMWARE:
				next = 0;

			case DYLD_CHAINED_PTR_32_CACHE:
				next = (chainValue & (0x3L << 30L)) >> 30L;  // 2 bits
				break;

			case DYLD_CHAINED_PTR_32_FIRMWARE:
				next = (chainValue & (0x3FL << 26L)) >> 26L;  // 6 bits
				break;
			default:
				break;
		}

		return next;
	}
}
